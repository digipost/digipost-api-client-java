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
package no.digipost.api.client.archive;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;
import no.digipost.api.client.representations.archive.ArchiveDocumentContent;
import no.digipost.api.client.representations.archive.Archives;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public interface ArchiveApi {

    Archives getArchives(SenderId senderId);

    CloseableHttpResponse sendMultipartArchive(HttpEntity build);

    Archive getArchiveDocuments(URI uri);

    Archive getArchiveDocumentByUUID(SenderId senderId, UUID uuid);

    void deleteArchiveDocumentByUUID(URI deleteArchiveDocumentUri);

    Archive addUniqueUUIDToArchiveDocument(SenderId senderId, UUID uuid, UUID newuuid);

    ArchiveDocument saveArchiveDocument(ArchiveDocument archiveDocument, URI uri);

    public static interface ArchivingDocuments {

        /**
         * Laster opp innhold til et dokument.
         *
         * @return videre operasjoner for å fullføre leveransen.
         */
        ArchivingDocuments addFile(ArchiveDocument document, InputStream content);

        /**
         * Laster opp innhold til et dokument.
         *
         * @return videre operasjoner for å fullføre leveransen.
         */
        default ArchivingDocuments addFile(ArchiveDocument document, byte[] content) {
            return addFile(document, new ByteArrayInputStream(content));
        }

        /**
         * Sender forsendelsen.
         */
        Archive send();
    }

    /**
     * Henter en liste av ArkivDokumenter med en gitt referanseId
     * @return En liste av arkiver som igjen holder en liste av arkivdokumenter med den gitte referanseID-en.
     */
    Archives getArchiveDocumentsByReferenceId(SenderId senderId, String referenceId);

    Archive getArchiveDocument(URI uri);

    ArchiveDocumentContent getArchiveDocumentContent(URI uri);

    InputStream getArchiveDocumentContentStream(URI uri);
}
