/*
 * Copyright (C) Posten Bring AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.api.client.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.digipost.api.client.BrokerId;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.http.client.HttpClientConnectionManagerFactory;
import no.digipost.http.client.HttpClientFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static no.digipost.api.client.errorhandling.ErrorCode.FAILED_TO_OBTAIN_ACCESS_TOKEN;

public class MutualTlsTokenProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MutualTlsTokenProvider.class);

    private static final Duration REFRESH_MARGIN = Duration.ofSeconds(30);
    private static final Duration MINIMUM_CACHE_TIME = Duration.ofSeconds(5);
    private static final Duration FALLBACK_TOKEN_LIFETIME = Duration.ofSeconds(60);

    private static final ObjectMapper JSON = new ObjectMapper();

    private final JwtAuthConfig config;
    private final Clock clock;
    private final CloseableHttpClient tokenClient;
    private final SSLContext sslContext;

    private final List<BasicNameValuePair> oAuthTokenEndpointParams;

    private volatile String cachedToken;
    private volatile Instant cacheValidUntil = Instant.MIN;
    private final Object refreshLock = new Object();

    public MutualTlsTokenProvider(JwtAuthConfig config, BrokerId brokerId, URI resourceServerUri, Clock clock) {
        this(config, brokerId, resourceServerUri, clock, null);
    }

    MutualTlsTokenProvider(JwtAuthConfig config, BrokerId brokerId, URI resourceServerUri, Clock clock, TrustManager[] trustManagers) {
        this.config = config;
        this.clock = clock;
        this.sslContext = buildSslContext(config, trustManagers);
        this.tokenClient = buildTokenClient(config, this.sslContext);
        this.oAuthTokenEndpointParams = createOAuth2TokenEndpointParams(config, brokerId, resourceServerUri);
    }

    public String getToken() {
        if (Instant.now(clock).isBefore(cacheValidUntil)) {
            return cachedToken;
        }
        synchronized (refreshLock) {
            if (Instant.now(clock).isBefore(cacheValidUntil)) {
                return cachedToken;
            }
            return fetchAndCacheToken();
        }
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    @Override
    public void close() {
        try {
            tokenClient.close();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to close the http client used for " + config.tokenEndpointUri, e);
        }
    }

    private String fetchAndCacheToken() {
        HttpPost request = new HttpPost(config.tokenEndpointUri);
        request.setEntity(new UrlEncodedFormEntity(oAuthTokenEndpointParams, StandardCharsets.UTF_8));

        try {
            return tokenClient.execute(request, response -> {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    HttpEntity responseEntity = response.getEntity();
                    if (responseEntity != null) {
                        String body = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                        throw new DigipostClientException(FAILED_TO_OBTAIN_ACCESS_TOKEN, "Token endpoint returned HTTP " + statusCode + " for " + config.tokenEndpointUri + ": " + body);
                    } else {
                        throw new DigipostClientException(FAILED_TO_OBTAIN_ACCESS_TOKEN, "Token endpoint returned HTTP " + statusCode + " for " + config.tokenEndpointUri);
                    }
                }

                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JsonNode tokenResponse = parseTokenResponse(responseBody);
                String token = extractAccessToken(tokenResponse);
                Instant expiry = resolveExpiry(token, tokenResponse);

                cachedToken = token;
                cacheValidUntil = resolveCacheValidUntil(Instant.now(clock), expiry);

                LOG.debug("Fetched new access token from {}, valid until {}, cached until {}", config.tokenEndpointUri, expiry, cacheValidUntil);
                return token;
            });
        } catch (IOException e) {
            throw new DigipostClientException(FAILED_TO_OBTAIN_ACCESS_TOKEN, "Failed to fetch access token from " + config.tokenEndpointUri, e);
        }
    }

    private static JsonNode parseTokenResponse(String responseBody) {
        try {
            return JSON.readTree(responseBody);
        } catch (IOException e) {
            throw new DigipostClientException(FAILED_TO_OBTAIN_ACCESS_TOKEN, "Could not parse token endpoint response as JSON", e);
        }
    }

    private static String extractAccessToken(JsonNode tokenResponse) {
        JsonNode accessToken = tokenResponse.get("access_token");
        if (accessToken == null || !accessToken.isTextual() || accessToken.asText().isEmpty()) {
            throw new DigipostClientException(FAILED_TO_OBTAIN_ACCESS_TOKEN, "Token endpoint response did not contain an 'access_token' field");
        }
        return accessToken.asText();
    }

    private Instant resolveExpiry(String accessToken, JsonNode tokenResponse) {
        JsonNode expiresIn = tokenResponse.get("expires_in");
        if (expiresIn != null && expiresIn.canConvertToLong()) {
            return Instant.now(clock).plusSeconds(expiresIn.asLong());
        }

        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length >= 2) {
                JsonNode payload = JSON.readTree(Base64.getUrlDecoder().decode(parts[1]));
                JsonNode exp = payload.get("exp");
                if (exp != null && exp.canConvertToLong()) {
                    return Instant.ofEpochSecond(exp.asLong());
                }
            }
        } catch (Exception e) {
            LOG.warn("Could not determine token expiry; caching for {} only. Reason: {}", FALLBACK_TOKEN_LIFETIME, e.getMessage());
        }

        return Instant.now(clock).plus(FALLBACK_TOKEN_LIFETIME);
    }

    static Instant resolveCacheValidUntil(Instant now, Instant expiry) {
        Instant refreshAt = expiry.minus(REFRESH_MARGIN);
        Instant minimum = now.plus(MINIMUM_CACHE_TIME);
        if (refreshAt.isAfter(minimum)) {
            return refreshAt;
        }
        return minimum.isBefore(expiry) ? minimum : expiry;
    }

    private static SSLContext buildSslContext(JwtAuthConfig config, TrustManager[] trustManagers) {
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(config.keyStore, config.keyPassword);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Could not build SSL context from keystore for " + config.tokenEndpointUri, e);
        }
    }

    private static CloseableHttpClient buildTokenClient(JwtAuthConfig config, SSLContext sslContext) {

        return HttpClientFactory.create(config.httpClientSettings,
                HttpClientConnectionManagerFactory.createBuilder(config.httpClientConnectionSettings)
                        .setTlsSocketStrategy(ClientTlsStrategyBuilder.create()
                                .setSslContext(sslContext)
                                .buildClassic())
                        .build());
    }

    private static List<BasicNameValuePair> createOAuth2TokenEndpointParams(JwtAuthConfig config, BrokerId brokerId, URI resourceServerUri){
        return Arrays.asList(
                new BasicNameValuePair("grant_type", "client_credentials"),
                new BasicNameValuePair("client_id", config.clientId),
                new BasicNameValuePair("scope", "dpost-api:" + brokerId.stringValue()),
                new BasicNameValuePair("resource", requireNonNull(resourceServerUri, "resourceServerUri cannot be null").toString())
        );
    }
}
