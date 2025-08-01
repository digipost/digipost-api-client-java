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

import no.digipost.api.client.BrokerId;
import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.accounts.PublicMailboxTag;
import no.digipost.api.client.representations.accounts.Tag;
import no.digipost.api.client.security.Signer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddTagEksempel {
    // Din virksomhets Digipost-kontoid
    private static final BrokerId AVSENDERS_KONTOID = BrokerId.of(10987);

    // Passordet sertifikatfilen er beskyttet med
    private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

    public static void main(final String[] args) throws IOException {

        // 1. Vi lager en Signer ved å lese inn sertifikatet du har knyttet til
        // din Digipost-konto (i .p12-formatet)
        Signer signer;
        try (InputStream sertifikatInputStream = lesInnSertifikat()) {
            signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, SERTIFIKAT_PASSORD);
        }

        // 2. Vi oppretter en DigipostClient
        DigipostClient client = new DigipostClient(DigipostClientConfig.newConfiguration().build(), AVSENDERS_KONTOID, signer);

        // 3. Vi oppretter et fødselsnummerobjekt
        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // 4. Vi oppretter en PublicMailboxTag
        PublicMailboxTag publicMailboxTag = new PublicMailboxTag(pin);

        // 5. Vi oppretter en Tag
        Tag tag = new Tag(publicMailboxTag);

        // 6. Vi lar klientbiblioteket sende requesten
        client.addTag(tag);
    }

    private static InputStream lesInnSertifikat() {
        try {
            // Leser inn sertifikatet
            return new FileInputStream(new File("/path/til/sertifikatfil.p12"));
        } catch (FileNotFoundException e) {
            // Håndter at sertifikatet ikke kunne leses!
            throw new RuntimeException("Kunne ikke lese sertifikatfil: " + e.getMessage(), e);
        }
    }

}
