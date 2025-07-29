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
package no.digipost.api.client.shareddocuments;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.shareddocuments.ShareDocumentsRequestState;
import no.digipost.api.client.representations.shareddocuments.SharedDocumentContent;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

public interface SharedDocumentsApi {

    /**
     * Gets the current state for an existing ShareDocumentsRequest. If no documents have
     * been shared, the state contains no documents or relevant timestamps. If documents
     * have been shared, the content of each document is available through the use of the
     * corresponding links.
     *
     * @param shareDocumentsRequestUuid The UUID of the primary document of the original ShareDocumentsRequest message
     * @return the state 
     */
    ShareDocumentsRequestState getShareDocumentsRequestState(SenderId senderId, UUID shareDocumentsRequestUuid);

    /**
     * Gets the content of a specific document as a stream of bytes.
     *
     * @param uri The URI of the SharedDocument returned as part of the ShareDocumentsRequestState
     * @return the shared document content as stream
     */
    InputStream getSharedDocumentContentStream(URI uri);

    /**
     * Gets the content of a specific document as a link to view on Digipost's domain.
     *
     * @param uri The URI of the SharedDocument returned as part of the ShareDocumentsRequestState
     * @return the shared document content with url
     */
    SharedDocumentContent getSharedDocumentContent(URI uri);

    /**
     *  Stops the sharing, after which you will no longer have access to the shared documents
     *
     * @param uri The URI of the StopSharing relation returned as part of the ShareDocumentsRequestState
     */
    CloseableHttpResponse stopSharing(SenderId senderId, URI uri);

}
