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

import no.digipost.api.client.BrokerId;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.http.client.HttpClientConnectionSettings;
import no.digipost.http.client.HttpClientSettings;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;
import static no.digipost.api.client.errorhandling.ErrorCode.FAILED_TO_OBTAIN_ACCESS_TOKEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MutualTlsTokenProviderTest {

    private static final String P12_RESOURCE = "client-cert.p12";
    private static final String P12_PASSWORD = "qwer1234";
    private static final String CLIENT_ID = "test-client";
    private static final BrokerId BROKER_ID = BrokerId.of(1234);
    private static final URI RESOURCE_SERVER_URI = URI.create("https://api.digipost.no");
    private static final Instant NOW = Instant.parse("2026-08-14T12:00:00Z");

    private TokenEndpointStub tokenEndpoint;
    private SettableClock clock;
    private final List<MutualTlsTokenProvider> tokenProviders = new ArrayList<>();

    @BeforeEach
    void startTokenEndpoint() throws Exception {
        tokenEndpoint = new TokenEndpointStub();
        clock = new SettableClock(NOW);
    }

    @AfterEach
    void closeTokenProvidersAndStopTokenEndpoint() {
        tokenProviders.forEach(MutualTlsTokenProvider::close);
        tokenProviders.clear();
        if (tokenEndpoint != null) {
            tokenEndpoint.close();
        }
    }

    @Test
    void henter_token_og_presenterer_klientsertifikatet_i_handshaken() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"the-token\",\"expires_in\":300}");

        assertThat(tokenProvider().getToken(), is("the-token"));

        Certificate[] presented = tokenEndpoint.certificatesPresentedByClient();
        assertThat("mIdP mottok ingen klientsertifikat – klienten presenterte ingenting i handshaken", presented, notNullValue());
        assertThat(presented[0], instanceOf(X509Certificate.class));
        assertThat(((X509Certificate) presented[0]).getSubjectX500Principal().getName(), containsString("sertifikat-TEST"));
    }

    @Test
    void sender_client_credentials_parametrene_til_token_endepunktet() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"the-token\",\"expires_in\":300}");

        tokenProvider().getToken();

        assertThat(parameter("grant_type"), is("client_credentials"));
        assertThat(parameter("client_id"), is(CLIENT_ID));
        assertThat(parameter("scope"), is("dpost-api:1234"));
        assertThat(parameter("resource"), is(RESOURCE_SERVER_URI.toString()));
    }

    @Test
    void cacher_tokenet_mellom_kall() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"the-token\",\"expires_in\":300}");
        MutualTlsTokenProvider tokenProvider = tokenProvider();

        tokenProvider.getToken();
        clock.advance(Duration.ofSeconds(100));

        assertThat(tokenProvider.getToken(), is("the-token"));
        assertThat(tokenEndpoint.receivedRequestCount(), is(1));
    }

    @Test
    void henter_nytt_token_naar_det_forrige_naermer_seg_utloep() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"first-token\",\"expires_in\":300}");
        MutualTlsTokenProvider tokenProvider = tokenProvider();

        tokenProvider.getToken();
        clock.advance(Duration.ofSeconds(280));
        tokenEndpoint.respondWith(200, "{\"access_token\":\"second-token\",\"expires_in\":300}");

        assertThat(tokenProvider.getToken(), is("second-token"));
        assertThat(tokenEndpoint.receivedRequestCount(), is(2));
    }

    @Test
    void henter_nytt_token_naar_det_forrige_er_invalidert() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"first-token\",\"expires_in\":300}");
        MutualTlsTokenProvider tokenProvider = tokenProvider();

        tokenProvider.getToken();
        tokenEndpoint.respondWith(200, "{\"access_token\":\"second-token\",\"expires_in\":300}");
        tokenProvider.invalidate("first-token");

        assertThat(tokenProvider.getToken(), is("second-token"));
        assertThat(tokenEndpoint.receivedRequestCount(), is(2));
    }

    @Test
    void beholder_tokenet_naar_et_annet_blir_invalidert() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"the-token\",\"expires_in\":300}");
        MutualTlsTokenProvider tokenProvider = tokenProvider();

        tokenProvider.getToken();
        tokenProvider.invalidate("a-token-already-replaced-by-the-cached-one");

        assertThat(tokenProvider.getToken(), is("the-token"));
        assertThat(tokenEndpoint.receivedRequestCount(), is(1));
    }

    @Test
    void bruker_exp_fra_tokenet_naar_expires_in_mangler() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"" + jwtExpiringAt(NOW.plus(300, SECONDS)) + "\"}");
        MutualTlsTokenProvider tokenProvider = tokenProvider();

        tokenProvider.getToken();
        clock.advance(Duration.ofSeconds(100));
        tokenProvider.getToken();
        assertThat("tokenet er gyldig i 300s, så det skal fortsatt være cachet", tokenEndpoint.receivedRequestCount(), is(1));

        clock.advance(Duration.ofSeconds(180));
        tokenProvider.getToken();
        assertThat(tokenEndpoint.receivedRequestCount(), is(2));
    }

    @Test
    void feil_fra_token_endepunktet_gir_DigipostClientException() throws Exception {
        tokenEndpoint.respondWith(503, "{\"error\":\"temporarily_unavailable\"}");

        DigipostClientException thrown = assertThrows(DigipostClientException.class, () -> tokenProvider().getToken());

        assertThat(thrown.getErrorCode(), is(FAILED_TO_OBTAIN_ACCESS_TOKEN));
        assertThat(thrown.getMessage(), containsString("503"));
    }

    /**
     * Statuskoder som ikke kan ha en responsbody gir ingen {@link HttpEntity} å lese
     * feilmeldingen fra, og {@link EntityUtils#toString(HttpEntity, java.nio.charset.Charset)}
     * kaster {@link NullPointerException} hvis den blir kalt med en null-entity.
     */
    @ParameterizedTest
    @ValueSource(ints = { 204, 304 })
    void feil_uten_responsbody_gir_DigipostClientException_og_ikke_NullPointerException(int statusUtenBody) throws Exception {
        tokenEndpoint.respondWithoutBody(statusUtenBody);

        Exception thrown = assertThrows(Exception.class, () -> tokenProvider().getToken());

        assertThat("EntityUtils.toString(..) ble kalt med responsens null-entity", thrown, not(instanceOf(NullPointerException.class)));
        assertThat(thrown, instanceOf(DigipostClientException.class));

        DigipostClientException clientException = (DigipostClientException) thrown;
        assertThat(clientException.getErrorCode(), is(FAILED_TO_OBTAIN_ACCESS_TOKEN));
        assertThat(clientException.getMessage(), containsString(String.valueOf(statusUtenBody)));
        assertThat(clientException.getMessage(), containsString(tokenEndpoint.tokenEndpointUri().toString()));
        assertThat("feilmeldingen skal ikke antyde at det fulgte med en body", clientException.getMessage(), not(containsString("null")));
    }

    @Test
    void svar_som_ikke_er_json_gir_DigipostClientException() throws Exception {
        tokenEndpoint.respondWith(200, "<html>not json</html>");

        DigipostClientException thrown = assertThrows(DigipostClientException.class, () -> tokenProvider().getToken());

        assertThat(thrown.getErrorCode(), is(FAILED_TO_OBTAIN_ACCESS_TOKEN));
    }

    @Test
    void svar_uten_access_token_gir_DigipostClientException() throws Exception {
        tokenEndpoint.respondWith(200, "{\"expires_in\":300}");

        DigipostClientException thrown = assertThrows(DigipostClientException.class, () -> tokenProvider().getToken());

        assertThat(thrown.getErrorCode(), is(FAILED_TO_OBTAIN_ACCESS_TOKEN));
        assertThat(thrown.getMessage(), containsString("access_token"));
    }

    @Test
    void bruker_timeoutene_som_er_konfigurert_for_token_klienten() throws Exception {
        tokenEndpoint.respondWith(200, "{\"access_token\":\"the-token\",\"expires_in\":300}");
        tokenEndpoint.delayResponsesBy(Duration.ofSeconds(2));

        JwtAuthConfig config = configBuilder()
                .tokenEndpointHttpSettings(HttpClientSettings.DEFAULT, HttpClientConnectionSettings.DEFAULT.socketTimeout(200))
                .build();

        DigipostClientException thrown = assertThrows(DigipostClientException.class, () -> tokenProvider(config).getToken());

        assertThat(thrown.getErrorCode(), is(FAILED_TO_OBTAIN_ACCESS_TOKEN));
        assertThat("token-klienten ventet lenger enn den konfigurerte socket-timeouten", thrown.getCause(), instanceOf(SocketTimeoutException.class));
    }

    private MutualTlsTokenProvider tokenProvider() throws Exception {
        return tokenProvider(configBuilder().build());
    }

    private MutualTlsTokenProvider tokenProvider(JwtAuthConfig config) throws Exception {
        MutualTlsTokenProvider tokenProvider = new MutualTlsTokenProvider(config, BROKER_ID, RESOURCE_SERVER_URI, clock, tokenEndpoint.trustManagers());
        tokenProviders.add(tokenProvider);
        return tokenProvider;
    }

    private JwtAuthConfig.Builder configBuilder() {
        return JwtAuthConfig
                .newConfig(CLIENT_ID)
                .tokenEndpoint(tokenEndpoint.tokenEndpointUri().toString())
                .pkcs12KeyStore(p12Stream(), P12_PASSWORD);
    }

    private String parameter(String name) {
        List<NameValuePair> form = tokenEndpoint.lastReceivedForm();
        return form.stream()
                .filter(parameter -> parameter.getName().equals(name))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Parameteren '" + name + "' ble ikke sendt. Mottok: " + form));
    }

    private static String jwtExpiringAt(Instant expiry) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(("{\"exp\":" + expiry.getEpochSecond() + "}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    private static InputStream p12Stream() {
        InputStream stream = MutualTlsTokenProviderTest.class.getResourceAsStream(P12_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Mangler testressurs " + P12_RESOURCE);
        }
        return stream;
    }
}
