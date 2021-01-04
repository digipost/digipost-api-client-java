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
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;
import no.digipost.api.client.security.Signer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ArkiverDokumenterEksempel {

    // Din virksomhets Digipost-kontoid
    private static final SenderId AVSENDERS_KONTOID = SenderId.of(10987);

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
        DigipostClient client = new DigipostClient(DigipostClientConfig.newConfiguration().build(),
                AVSENDERS_KONTOID.asBrokerId(), signer);

        // 3. Vi beskriver to dokumenter du ønsker å arkivere i ditt arkiv
        final ArchiveDocument faktura = new ArchiveDocument(
                UUID.randomUUID()
                , "faktura_123123.pdf"
                , "pdf"
                , "application/pdf"
                , "234234235234235"
        );
        final ArchiveDocument vedlegg = new ArchiveDocument(
                UUID.randomUUID()
                , "vedlegg_123123.pdf"
                , "pdf"
                , "application/pdf"
                , "234234235234235"
        );

        // 4. Vi oppretter arkivmeldingen med dokumentene
        Archive archive = Archive.defaultArchive()
                .documents(faktura, vedlegg)
                .build();

        // 5. Vi får klienten til å lese inn filene og sende dem til arkivering
        client.archiveDocuments(archive)
                .addFile(faktura, readFileFromDisk("faktura_1234.pdf"))
                .addFile(vedlegg, readFileFromDisk("vedlegg_1234.pdf"))
                .send();
    }

    private static InputStream readFileFromDisk(String filename) {
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
