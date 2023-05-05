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
package no.digipost.api.client.document;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.representations.DocumentStatus;
import no.digipost.api.client.representations.Link;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.UUID;

public interface DocumentApi {

    /**
     * Henter status på dokumeter som tidligere blitt sendt i Digipost, både via digital og print-kanal.
     */
    DocumentStatus getDocumentStatus(SenderId senderId, UUID uuid);

    /**
     * Henter status på dokumeter som tidligere blitt sendt i Digipost, både via digital og print-kanal.
     */
    DocumentStatus getDocumentStatus(Link linkToDocumentStatus);

    /**
     * Retrieve the content of a document.
     *
     * @param path the path to the document resource
     * @return the bytes of the document
     */
    InputStream getDocumentContent(String path);

    /**
     * Henter hendelser knyttet til tidligere sendte brev.
     *
     * @param organisation Organisasjonsnummer
     * @param partId Frivillig organisasjons-enhet, kan være {@code null}
     *
     */
    DocumentEvents getDocumentEvents(String organisation, String partId, SenderId senderId, ZonedDateTime from, ZonedDateTime to, int offset, int maxResults);

}
