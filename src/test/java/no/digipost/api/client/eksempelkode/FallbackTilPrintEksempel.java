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
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageRecipient;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.PrintDetails;
import no.digipost.api.client.representations.PrintRecipient;
import no.digipost.api.client.representations.SmsNotification;
import no.digipost.api.client.security.jwt.JwtAuthConfig;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.UUID;

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.PrintDetails.NondeliverableHandling.RETURN_TO_SENDER;
import static no.digipost.api.client.representations.PrintDetails.PrintColors.MONOCHROME;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

/**
 * Kode som brukes i dokumentasjonen for klientbiblioteket.
 *
 */
public class FallbackTilPrintEksempel {
    // Din virksomhets Digipost-kontoid
    private static final SenderId AVSENDERS_KONTOID = SenderId.of(10987);

    // Klient-IDen du fikk da du registrerte klienten hos Digipost
    private static final String KLIENT_ID = "din-klient-id";

    // Passordet klientsertifikatet er beskyttet med
    private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

    public static void main(final String[] args) throws IOException {

        // 1. For å kunne kryptere brevet som skal sendes trenger vi
        // BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        // 2. Vi setter opp autentiseringen ved å lese inn klientsertifikatet
        // (i .p12-formatet) som brukes i mTLS-handshaken mot token-endepunktet
        JwtAuthConfig jwtAuthConfig;
        try (InputStream sertifikatInputStream = lesInnKlientsertifikat()) {
            jwtAuthConfig = JwtAuthConfig
                    .newConfig(KLIENT_ID)
                    .pkcs12KeyStore(sertifikatInputStream, SERTIFIKAT_PASSORD)
                    .build();
        }

        // 3. Vi oppretter en DigipostClient
        DigipostClient client = DigipostClient.withJwtMtlsAuthentication(DigipostClientConfig.newConfiguration().build(),
                                                   AVSENDERS_KONTOID.asBrokerId(), jwtAuthConfig);

        // 4. Vi oppretter et fødselsnummerobjekt som skal brukes til å
        // identifisere mottaker i Digipost
        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // 5. Vi oppretter en forsendelse for sending av brevet i Digipost og med adresseinformasjon som vil
        // benyttes dersom mottaker ikke er Digipostbruker
        Document primaryDocument = new Document(UUID.randomUUID(), "Dokumentets emne", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL);

        PrintDetails printDetails = new PrintDetails(new PrintRecipient("Mottakers navn", new NorwegianAddress("postnummer","Mottakers poststed")),
                new PrintRecipient("Avsenders navn", new NorwegianAddress("postnummer", "Avsenders poststed")), MONOCHROME, RETURN_TO_SENDER);
        UUID dinForsendelseId = UUID.randomUUID();
        Message message = Message.newMessage(dinForsendelseId, primaryDocument)
                .recipient(new MessageRecipient(pin, printDetails))
                .build();

        // 7. Foreløpig støtter Digipost kun å sende krypterte brev til print. Kaller du på
        // encrypt() så vil klientbiblioteket krypterer filen for
        // deg før den oversendes Digipost.
        primaryDocument.encrypt();

        // 8. Vi oppretter forsendelsen, legger til innhold og alternativt
        // innhold for print, og til slutt sender forsendelsen. Alt håndteres
        // av klientbiblioteket.
        client.createMessage(message)
              .addContent(primaryDocument, getMessageContent(), getPrintContent())
              .send();

    }

    private static InputStream getMessageContent() {
        // Her må du returnere brevinnholdet du ønsker for Digipost å sende istedenfor null
        return null;
    }

    private static InputStream getPrintContent() {
        // Her må du returnere brevinnholdet du ønsker for print å sende istedenfor null
        return null;
    }

    private static InputStream lesInnKlientsertifikat() {
        try {
            // Leser inn klientsertifikatet selv med Apache Commons FileUtils.
            return FileUtils.openInputStream(new File("/path/til/klientsertifikat.p12"));
        } catch (IOException e) {
            // Håndter at klientsertifikatet ikke kunne leses!
            throw new RuntimeException("Kunne ikke lese klientsertifikatet");
        }
    }
}
