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

package no.digipost.api.client;

import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;

import java.io.InputStream;

public class InboxCommunicator extends Communicator{

    public InboxCommunicator(final ApiService apiService, final EventLogger eventLogger) {
        super(apiService, eventLogger);
    }

    public Inbox getInbox(SenderId senderId) {
        return apiService.getInbox(senderId);
    }

    public InputStream getInboxDocuInboxmentContentStream(InboxDocument inboxDocument) {
        return apiService.getInboxDocumentContentStream(inboxDocument);
    }

    public void deleteInboxDocument(InboxDocument inboxDocument) {
        apiService.deleteInboxDocument(inboxDocument);
    }
}
