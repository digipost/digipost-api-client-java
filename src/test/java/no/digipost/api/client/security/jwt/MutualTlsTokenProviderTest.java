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

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ssl.TLS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

public class MutualTlsTokenProviderTest {

    private static final String P12_RESOURCE = "client-cert.p12";
    private static final String P12_PASSWORD = "qwer1234";

    private HttpsServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void presenterer_klientsertifikat_i_mtls_handshake() throws Exception {
        JwtAuthConfig config = JwtAuthConfig
                .newConfig("test-client")
                .pkcs12KeyStore(p12Stream(), P12_PASSWORD)
                .build();

        AtomicReference<Certificate[]> presentedByClient = new AtomicReference<>();
        URI tokenEndpoint = startTokenServer(config, presentedByClient);

        try (CloseableHttpClient client = clientPresentingConfiguredCertificate(config)) {
            client.execute(new HttpGet(tokenEndpoint), response -> {
                EntityUtils.consume(response.getEntity());
                return null;
            });
        }

        Certificate[] presented = presentedByClient.get();
        assertThat("mIdP mottok ingen klientsertifikat – klienten presenterte ingenting i handshaken", presented, notNullValue());
        assertThat(presented[0], instanceOf(X509Certificate.class));
    }

    private CloseableHttpClient clientPresentingConfiguredCertificate(JwtAuthConfig config) throws Exception {
        SSLContext clientContext = SSLContext.getInstance("TLS");
        clientContext.init(keyManagers(config), new TrustManager[]{ TRUST_ALL }, null);

        PoolingHttpClientConnectionManagerBuilder connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                        .setSslContext(clientContext)
                        .setTlsVersions(TLS.V_1_2)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build());

        return HttpClients.custom()
                .setConnectionManager(connectionManager.build())
                .build();
    }

    private URI startTokenServer(JwtAuthConfig config, AtomicReference<Certificate[]> presentedByClient) throws Exception {
        SSLContext serverContext = SSLContext.getInstance("TLS");
        serverContext.init(
                keyManagers(config), // server presents the .p12 cert
                new TrustManager[]{ TRUST_ALL },
                null
        );

        server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(serverContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLParameters sslParameters = serverContext.getDefaultSSLParameters();
                sslParameters.setProtocols(new String[]{ "TLSv1.2" });
                sslParameters.setWantClientAuth(true);
                params.setSSLParameters(sslParameters);
            }
        });
        server.createContext("/token", exchange -> {
            SSLSession sslSession = ((com.sun.net.httpserver.HttpsExchange) exchange).getSSLSession();
            try {
                presentedByClient.set(sslSession.getPeerCertificates());
            } catch (SSLPeerUnverifiedException e) {
                presentedByClient.set(null);
            }
            byte[] body = "{\"access_token\":\"t\",\"expires_in\":300}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        return URI.create("https://127.0.0.1:" + server.getAddress().getPort() + "/token");
    }

    private static KeyManager[] keyManagers(JwtAuthConfig config) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(config.keyStore, config.keyPassword);
        return keyManagerFactory.getKeyManagers();
    }

    private static final X509TrustManager TRUST_ALL = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) { }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) { }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private InputStream p12Stream() {
        InputStream stream = getClass().getResourceAsStream(P12_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Mangler testressurs " + P12_RESOURCE + " – legg den vedlagte .p12-filen under src/test/resources/no/digipost/api/client/security/jwt/");
        }
        return stream;
    }
}
