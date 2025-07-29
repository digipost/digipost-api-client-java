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
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;
import no.digipost.api.client.representations.archive.ArchiveDocumentContent;
import no.digipost.api.client.representations.archive.Archives;
import no.digipost.api.client.security.Signer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class GithubPagesArchiveExamples {

    private DigipostClient client;

    public void set_up_client() throws FileNotFoundException {
        SenderId senderId = SenderId.of(10987);

        DigipostClient client = new DigipostClient(
                DigipostClientConfig.newConfiguration().build(),
                senderId.asBrokerId(),
                Signer.usingKeyFromPKCS12KeyStore(new FileInputStream("certificate.p12"), "TheSecretPassword"));
    }

    public void get_list_of_archives() throws IOException {
        //get a list of the archives
        Archives archives = client.getArchives(SenderId.of(123456));
    }
    
    public void get_documents_from_archive(){
        Archives archives = client.getArchives(SenderId.of(123456));
        Archive defaultArchive = archives.getArchives().get(0);

        final List<ArchiveDocument> first100 = defaultArchive.getNextDocuments()
                .map(client::getArchiveDocuments)
                .map(Archive::getDocuments)
                .orElse(Collections.emptyList());
    }
    
    public void get_documents_by_attributes(){
        Archives archives = client.getArchives(SenderId.of(123456));
        Archive defaultArchive = archives.getArchives().get(0);
        
        defaultArchive.getNextDocumentsWithAttributes(Map.of("Key", "Value"));
    }

    public void get_documents_by_date_with_attributes_by_date(){
        Archives archives = client.getArchives(SenderId.of(123456));
        Archive defaultArchive = archives.getArchives().get(0);

        defaultArchive.getNextDocumentsWithAttributesByDate(Map.of("Key", "Value"), OffsetDateTime.now().minus(Period.ofDays(4)), OffsetDateTime.now());
    }

    public void get_documents_by_date(){
        Archives archives = client.getArchives(SenderId.of(123456));
        Archive defaultArchive = archives.getArchives().get(0);
        
        defaultArchive.getNextDocumentsByDate(OffsetDateTime.now().minus(Period.ofDays(4)), OffsetDateTime.now());
    }

    public void example_of_iteration_of_all_documents(){
        final Archives archives = client.getArchives(SenderId.of(123456));

        Archive current = archives.getArchives().get(0);
        List<ArchiveDocument> documents = new ArrayList<>();

        while (current.getNextDocuments().isPresent()) {
            current = current.getNextDocuments()
                    .map(client::getArchiveDocuments)
                    .orElse(new Archive());

            documents.addAll(current.getDocuments());
        }

        System.out.println(documents);
    }

    public void get_documents_by_referenceid_with_content() {
        final String referenceId = "REFERENCE_ID";
        Archives archives = client.getArchiveDocumentsByReferenceId(SenderId.of(123456), referenceId);
        Archive firstArchive = archives.getArchives().get(0);

        ArchiveDocument firstDocument = firstArchive.getDocuments().get(0);
        URI getDocumentContentURI = firstDocument.getDocumentContent();
        URI getDocumentContentStreamURI = firstDocument.getDocumentContentStream();

        ArchiveDocumentContent content = client.getArchiveDocumentContent(getDocumentContentURI);
        InputStream contentStream = client.getArchiveDocumentContentStream(getDocumentContentStreamURI);
    }
    
    public void get_documents_by_uuid() {
        // Send in file
        final UUID uuidForUniqueDocument = UUID.fromString("vedlegg:123123:txt");
        
        final ArchiveDocument vedlegg = new ArchiveDocument(
                uuidForUniqueDocument
                , "vedlegg_123123.txt"
                , "txt"
                , "text/plain"
        ).withDeleteAfter(Period.ofYears(3), Clock.system(ZoneId.of("Europe/Oslo")));

        Archive archive = Archive.defaultArchive()
                .documents(vedlegg, vedlegg)
                .build();

        client.archiveDocuments(archive)
                .addFile(vedlegg, "filecontent in .txt".getBytes())
                .send();
        
        //retrieve file
        client.getArchiveDocumentByUuid(uuidForUniqueDocument);
        // also as broker:
        client.getArchiveDocumentByUuid(SenderId.of(123456), uuidForUniqueDocument);
    }
    
    public void add_unique_uuid_to_document(){
        final UUID uuidForUniqueDocument = UUID.fromString("vedlegg_filen:txt");
        final UUID alsoKnownAs = UUID.fromString("id:123123");
        
        client.addUniqueUUIDToArchiveDocument(SenderId.of(123456), uuidForUniqueDocument, alsoKnownAs);
    }
    
}
