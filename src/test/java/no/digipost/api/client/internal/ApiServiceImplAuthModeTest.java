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
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.security.jwt.JwtAuthConfig;
import no.digipost.http.client.HttpClientFactory;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;

import static no.digipost.api.client.DigipostClientConfig.newConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApiServiceImplAuthModeTest {

    private static final BrokerId BROKER_ID = BrokerId.of(1234);
    private static final String P12_RESOURCE = "/no/digipost/api/client/security/jwt/client-cert.p12";
    private static final String P12_PASSWORD = "qwer1234";

    private static final Signer DUMMY_SIGNER = dataToSign -> new byte[0];

    @Test
    void kaster_feil_naar_verken_signer_eller_jwtAuthConfig_er_konfigurert() {
        DigipostClientConfig config = newConfiguration().build();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                new ApiServiceImpl(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, null, null));

        assertThat(thrown.getMessage(), containsString("må konfigureres"));
    }

    @Test
    void kaster_feil_naar_baade_signer_og_jwtAuthConfig_er_konfigurert() {
        DigipostClientConfig config = newConfiguration().build();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                new ApiServiceImpl(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, DUMMY_SIGNER, jwtAuthConfig()));

        assertThat(thrown.getMessage(), containsString("kan ikke konfigureres med både"));
    }

    @Test
    void bygger_klient_naar_kun_jwtAuthConfig_er_konfigurert() {
        DigipostClientConfig config = newConfiguration().build();

        assertDoesNotThrow(() ->
                new ApiServiceImpl(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, null, jwtAuthConfig()));
    }

    @Test
    void bygger_klient_naar_kun_signer_er_konfigurert() {
        DigipostClientConfig config = newConfiguration().build();

        assertDoesNotThrow(() ->
                new ApiServiceImpl(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, DUMMY_SIGNER, null));
    }

    private static JwtAuthConfig jwtAuthConfig() {
        return JwtAuthConfig
                .newConfig(URI.create("https://idp.example.com/token"), URI.create("https://api.digipost.no"), "test-client")
                .pkcs12KeyStore(p12Stream(), P12_PASSWORD)
                .build();
    }

    private static InputStream p12Stream() {
        InputStream stream = ApiServiceImplAuthModeTest.class.getResourceAsStream(P12_RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Mangler testressurs " + P12_RESOURCE);
        }
        return stream;
    }
}
