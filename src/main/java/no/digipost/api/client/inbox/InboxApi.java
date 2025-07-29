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
package no.digipost.api.client.inbox;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;

import java.io.InputStream;

public interface InboxApi {

    /**
     * Get documents from the inbox for the organisation represented by senderId.
     *
     * @param senderId Either an organisation that you operate on behalf of or your brokerId
     * @param offset Number of documents to skip. For pagination
     * @param limit Maximum number of documents to retrieve (max 1000)
     * @return Inbox element with the n=limit first documents
     */
    Inbox getInbox(SenderId senderId, int offset, int limit);

    /**
     * Get the content of a document as a stream. The content is streamed from the server so remember to
     * close the stream to prevent connection leaks.
     *
     * @param inboxDocument The document to get content for
     * @return Entire content of the document as a stream
     */
    InputStream getInboxDocumentContentStream(InboxDocument inboxDocument);

    /**
     * Delets the given document from the server
     *
     * @param inboxDocument The document to delete
     */
    void deleteInboxDocument(InboxDocument inboxDocument);

}
