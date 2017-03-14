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
import no.digipost.api.client.delivery.ApiFlavor;
import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class GithubPagesReceiveExamples {

    private static final long SENDER_ID = 1;
    private static final String UUID1 = UUID.randomUUID().toString();
    private static final String UUID2 = UUID.randomUUID().toString();
    private static final String UUID3 = UUID.randomUUID().toString();
    private static final String UUID4 = UUID.randomUUID().toString();
    private static final String CERTIFICATE_PASSWORD = "passord";

    public void set_up_client() throws FileNotFoundException {
        long senderId = 123456;

        DigipostClient client = new DigipostClient(
                new DigipostClientConfig.DigipostClientConfigBuilder().build(),
                ApiFlavor.ATOMIC_REST,
                "https://api.digipost.no",
                senderId,
                new FileInputStream("certificate.p12"), "TheSecretPassword");

    }

    public void get_documents_in_inbox() throws IOException {
        long senderId = 123456;

        DigipostClient client = new DigipostClient(
                new DigipostClientConfig.DigipostClientConfigBuilder().build(),
                ApiFlavor.ATOMIC_REST,
                "https://api.digipost.no",
                senderId,
                new FileInputStream("certificate.p12"), "TheSecretPassword");

        //get first 100 documents
        final Inbox first100 = client.getInbox(new SenderId(senderId), 0, 100);

        //get next 100 documents
        final Inbox next100 = client.getInbox(new SenderId(senderId), 100, 100);
    }

    public void download_document_content() throws IOException {
        long senderId = 123456;

        DigipostClient client = new DigipostClient(
                new DigipostClientConfig.DigipostClientConfigBuilder().build(),
                ApiFlavor.ATOMIC_REST,
                "https://api.digipost.no",
                senderId,
                new FileInputStream("certificate.p12"), "TheSecretPassword");

        final Inbox inbox = client.getInbox(new SenderId(senderId));

        final InboxDocument documentMetadata = inbox.documents.get(0);

        System.out.println("Content type is: " + documentMetadata.getContentType());
        final InputStream documentContent = client.getInboxDocumentContent(documentMetadata);
    }

    public void delete_document() throws IOException {
        long senderId = 123456;

        DigipostClient client = new DigipostClient(
                new DigipostClientConfig.DigipostClientConfigBuilder().build(),
                ApiFlavor.ATOMIC_REST,
                "https://api.digipost.no",
                senderId,
                new FileInputStream("certificate.p12"), "TheSecretPassword");

        final Inbox inbox = client.getInbox(new SenderId(senderId));

        final InboxDocument documentMetadata = inbox.documents.get(0);

        client.deleteInboxDocument(documentMetadata);
    }

    public void download_attachment_content() throws IOException {
        long senderId = 123456;

        DigipostClient client = new DigipostClient(
                new DigipostClientConfig.DigipostClientConfigBuilder().build(),
                ApiFlavor.ATOMIC_REST,
                "https://api.digipost.no",
                senderId,
                new FileInputStream("certificate.p12"), "TheSecretPassword");

        final Inbox inbox = client.getInbox(new SenderId(senderId));

        final InboxDocument documentMetadata = inbox.documents.get(0);
        final InboxDocument attachment = documentMetadata.getAttachments().get(0);

        System.out.println("Content type is: " + attachment.getContentType());
        final InputStream attachmentContent = client.getInboxDocumentContent(attachment);
    }
}
