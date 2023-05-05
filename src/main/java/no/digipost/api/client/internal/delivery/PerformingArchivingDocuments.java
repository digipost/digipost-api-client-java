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
package no.digipost.api.client.internal.delivery;

import no.digipost.api.client.archive.ArchiveApi;
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PerformingArchivingDocuments implements ArchiveApi.ArchivingDocuments {
    private final Archive archive;
    private final ArchiveDeliverer archiveDeliverer;
    private final Map<UUID, DocumentContent> documents = new LinkedHashMap<>();

    public PerformingArchivingDocuments(Archive archive, ArchiveDeliverer archiveDeliverer) {
        this.archive          = archive;
        this.archiveDeliverer = archiveDeliverer;
    }

    @Override
    public ArchiveApi.ArchivingDocuments addFile(ArchiveDocument document, InputStream content) {
        documents.put(document.uuid, DocumentContent.CreateBothStreamContent(content));
        return this;
    }

    @Override
    public Archive send() {
        return archiveDeliverer.sendMultipartMessage(archive, documents);
    }
}
