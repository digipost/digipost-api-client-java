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
package no.digipost.api.client.eksempelkode;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.Suggestion;
import no.digipost.api.client.security.jwt.JwtAuthConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Kode som brukes i dokumentasjonen for klientbiblioteket.
 *
 */
@SuppressWarnings("unused")
public class AutocompleteEksempel {
    // Din virksomhets Digipost-kontoid
    private static final SenderId AVSENDERS_KONTOID = SenderId.of(10987);

    // Klient-IDen du fikk da du registrerte klienten hos Digipost
    private static final String KLIENT_ID = "din-klient-id";

    // Passordet klientsertifikatet er beskyttet med
    private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

    public static void main(final String[] args) throws IOException {

        // 1. Vi setter opp autentiseringen ved å lese inn klientsertifikatet
        // (i .p12-formatet) som brukes i mTLS-handshaken mot token-endepunktet
        JwtAuthConfig jwtAuthConfig;
        try (InputStream sertifikatInputStream = lesInnKlientsertifikat()) {
            jwtAuthConfig = JwtAuthConfig
                    .newConfig(KLIENT_ID)
                    .pkcs12KeyStore(sertifikatInputStream, SERTIFIKAT_PASSORD)
                    .build();
        }

        // 2. Vi oppretter en DigipostClient
        DigipostClient client = DigipostClient.withJwtMtlsAuthentication(DigipostClientConfig.newConfiguration().build(),
                                                   AVSENDERS_KONTOID.asBrokerId(), jwtAuthConfig);

        // 3. Vi ber om forslag til autofullføring
        List<Suggestion> suggestions = client.getAutocompleteSuggestions("Gunn").getSuggestions();

    }

    private static InputStream getMessageContent() {
        // Her må du returnere brevinnholdet du ønsker å sende istedenfor null
        return null;
    }

    private static InputStream lesInnKlientsertifikat() {
        try {
            // Leser inn klientsertifikatet
            return new FileInputStream(new File("/path/til/klientsertifikat.p12"));
        } catch (FileNotFoundException e) {
            // Håndter at klientsertifikatet ikke kunne leses!
            throw new RuntimeException("Kunne ikke lese klientsertifikatet: " + e.getMessage(), e);
        }
    }
}
