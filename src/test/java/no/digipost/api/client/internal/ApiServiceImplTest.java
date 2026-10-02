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
package no.digipost.api.client.internal;

import no.digipost.api.client.BrokerId;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.security.jwt.JwtAuthConfig;
import no.digipost.api.client.security.jwt.MutualTlsTokenProvider;
import no.digipost.http.client.HttpClientFactory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static no.digipost.api.client.DigipostClientConfig.newConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiServiceImplTest {

    private static final BrokerId BROKER_ID = BrokerId.of(1234);
    private static final String P12_RESOURCE = "/no/digipost/api/client/security/jwt/client-cert.p12";
    private static final String P12_PASSWORD = "qwer1234";

    @Test
    void bygger_jwt_autentiserende_klient() {
        DigipostClientConfig config = newConfiguration().build();

        assertDoesNotThrow(() ->
                ApiServiceImpl.create(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, jwtAuthConfig()));
    }

    @Test
    void krever_jwtAuthConfig_for_jwt_basert_autentisering() {
        DigipostClientConfig config = newConfiguration().build();

        assertThrows(NullPointerException.class, () ->
                ApiServiceImpl.create(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, null));
    }

    @Test
    void krever_token_provider() {
        DigipostClientConfig config = newConfiguration().build();

        assertThrows(NullPointerException.class, () ->
                ApiServiceImpl.withMutualTlsTokenProvider(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, null));
    }

    @Test
    void lukker_ogsaa_token_provideren_sin_http_klient() {
        DigipostClientConfig config = newConfiguration().build();
        MutualTlsTokenProvider tokenProvider = new MutualTlsTokenProvider(jwtAuthConfig(), BROKER_ID, config.digipostApiUri, config.clock);
        ApiServiceImpl apiService = ApiServiceImpl.withMutualTlsTokenProvider(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, tokenProvider);

        apiService.close();

        assertThrows(IllegalStateException.class, tokenProvider::getToken,
                "token provideren har fortsatt en åpen http-klient, og lekker connection poolen sin");
    }

    private static JwtAuthConfig jwtAuthConfig() {
        return JwtAuthConfig
                .newConfig("test-client")
                // ingen skal svare her: testene under skal aldri komme så langt som til å gjøre et kall
                .tokenEndpoint("https://localhost:1/oauth2/token")
                .pkcs12KeyStore(p12Stream(), P12_PASSWORD)
                .build();
    }

    private static InputStream p12Stream() {
        InputStream stream = ApiServiceImplTest.class.getResourceAsStream(P12_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Mangler testressurs " + P12_RESOURCE);
        }
        return stream;
    }
}
