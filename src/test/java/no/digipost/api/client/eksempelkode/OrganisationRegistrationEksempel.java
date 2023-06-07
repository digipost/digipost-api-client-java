/*
 * Copyright (C) Posten Norge AS
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
import no.digipost.api.client.representations.organisation.Language;
import no.digipost.api.client.representations.organisation.OrganisationRegistration;
import no.digipost.api.client.representations.organisation.OrganisationRegistrationAddressInformation;
import no.digipost.api.client.representations.organisation.OrganisationRegistrationTrustee;
import no.digipost.api.client.security.Signer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class OrganisationRegistrationEksempel {
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

        // 3. Vi oppretter et virksomhetsobjekt
        OrganisationRegistrationTrustee administrator = new OrganisationRegistrationTrustee("Admin", "Admin", "26079833787");
        OrganisationRegistrationAddressInformation addressInformation = new OrganisationRegistrationAddressInformation("Address", "7049", "Oslo");
        OrganisationRegistration organisation = new OrganisationRegistration(
                "Navn",              // Navn på virksomheten
                "123456789",         // Organisjonsnummer
                true,                // La andre sende digital post til virksomheten
                "99999999",          // Telefonnummer
                "email@example.com", // Epostadresse
                Language.BOKMAL,     // Språk
                administrator,       // Administrator for virksomheten
                addressInformation   // Virksomhetens adresse
        );

        // 6. Vi lar klientbiblioteket sende requesten
        client.registerOrganisation(organisation);
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
