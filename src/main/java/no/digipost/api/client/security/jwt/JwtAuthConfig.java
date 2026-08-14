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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static java.util.Objects.requireNonNull;

/**
 * Configures how the client obtains OAuth 2.0 access tokens over a mutual-TLS channel:
 * which token endpoint to ask, which client to identify as, and which client certificate
 * to present in the handshake.
 * <p>
 * The resource the tokens are requested for is <em>not</em> configured here. It is derived
 * from {@link no.digipost.api.client.DigipostClientConfig#digipostApiUri}, so that the
 * tokens are always issued for the same API the client actually talks to.
 */
public final class JwtAuthConfig {

    public final URI tokenEndpointUri;
    public final String clientId;
    final KeyStore keyStore;
    final char[] keyPassword;

    public static Builder newConfig(String clientId) {
        return new Builder(clientId);
    }

    public static class Builder {
        private URI tokenEndpointUri = URI.create("https://midp.digipost.no/oauth2/token");
        private final String clientId;
        private KeyStore keyStore;
        private char[] keyPassword;

        private Builder(String clientId) {
            this.clientId = requireNonNull(clientId, "clientId cannot be null");
        }

        public Builder tokenEndpoint(String tokenEndpoint) {
            this.tokenEndpointUri = URI.create(tokenEndpoint);
            return this;
        }

        public Builder pkcs12KeyStore(InputStream pkcs12Stream, String password) {
            requireNonNull(pkcs12Stream, "pkcs12Stream cannot be null");
            requireNonNull(password, "password cannot be null");
            try {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(pkcs12Stream, password.toCharArray());
                this.keyStore = ks;
                this.keyPassword = password.toCharArray();
                return this;
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                throw new IllegalArgumentException("Could not load PKCS12 keystore", e);
            }
        }

        public Builder keyStore(KeyStore keyStore, String keyPassword) {
            this.keyStore = requireNonNull(keyStore, "keyStore cannot be null");
            this.keyPassword = requireNonNull(keyPassword, "keyPassword cannot be null").toCharArray();
            return this;
        }

        public JwtAuthConfig build() {
            requireNonNull(keyStore, "A keyStore is required. Call pkcs12KeyStore() or keyStore().");
            return new JwtAuthConfig(tokenEndpointUri, clientId, keyStore, keyPassword);
        }
    }

    private JwtAuthConfig(URI tokenEndpointUri, String clientId, KeyStore keyStore, char[] keyPassword) {
        this.tokenEndpointUri = tokenEndpointUri;
        this.clientId = clientId;
        this.keyStore = keyStore;
        this.keyPassword = keyPassword;
    }
}
