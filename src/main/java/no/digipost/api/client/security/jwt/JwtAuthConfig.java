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

public final class JwtAuthConfig {

    public final URI tokenEndpointUri;
    public final URI resourceServerUri;
    public final String clientId;
    final KeyStore keyStore;
    final char[] keyPassword;

    public static Builder newConfig(URI tokenEndpointUri, URI resourceServerUri, String clientId) {
        return new Builder(tokenEndpointUri, resourceServerUri, clientId);
    }

    public static class Builder {
        private final URI tokenEndpointUri;
        private final URI resourceServerUri;
        private final String clientId;
        private KeyStore keyStore;
        private char[] keyPassword;

        private Builder(URI tokenEndpointUri, URI resourceServerUri, String clientId) {
            this.tokenEndpointUri = requireNonNull(tokenEndpointUri, "tokenEndpointUri cannot be null");
            this.resourceServerUri = requireNonNull(resourceServerUri, "resourceServerUri cannot be null");
            this.clientId = requireNonNull(clientId, "clientId cannot be null");
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
            return new JwtAuthConfig(tokenEndpointUri, resourceServerUri, clientId, keyStore, keyPassword);
        }
    }

    private JwtAuthConfig(URI tokenEndpointUri, URI resourceServerUri, String clientId, KeyStore keyStore, char[] keyPassword) {
        this.tokenEndpointUri = tokenEndpointUri;
        this.resourceServerUri = resourceServerUri;
        this.clientId = clientId;
        this.keyStore = keyStore;
        this.keyPassword = keyPassword;
    }
}
