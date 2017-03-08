/**
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

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.SmsNotification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import static no.digipost.api.client.DigipostClientConfig.DigipostClientConfigBuilder.newBuilder;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

/**
 * Kode som brukes i dokumentasjonen for klientbiblioteket.
 */
public class ForsendelseEksempel {

    // Din virksomhets Digipost-kontoid
    private static final long AVSENDERS_KONTOID = 10987;

    // Passordet sertifikatfilen er beskyttet med
    private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

    public static void main(final String[] args) {

        // 1. Vi leser inn sertifikatet du har knyttet til din Digipost-konto (i
        // .p12-formatet)
        InputStream sertifikatInputStream = lesInnSertifikat();

		// 2. Vi oppretter en DigipostClient
		DigipostClient client = new DigipostClient(newBuilder().build(), "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

        // 3. Vi oppretter et fødselsnummerobjekt
        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // 4. Vi oppretter hoveddokumentet
        Document primaryDocument = new Document(UUID.randomUUID().toString(), "Dokumentets emne",
                PDF, null, new SmsNotification(1),
                null,
                PASSWORD, NORMAL);

        // 5. Vi opprettet en forsendelse
        Message message = newMessage(null, primaryDocument)
                .personalIdentificationNumber(pin)
                .build();

        // 6. Vi lar klientbiblioteket håndtere opprettelse av forsendelse, legge til innhold,
        // og til slutt å sende forsendelsen
        client.createMessage(message)
                .addContent(primaryDocument, getPrimaryDocumentContent())
                .send();
    }

    private static InputStream getPrimaryDocumentContent() {
        // Her må du returnere brevinnholdet du ønsker å sende istedenfor null
        return null;
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
