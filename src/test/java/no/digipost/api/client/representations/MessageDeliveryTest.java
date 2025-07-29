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
package no.digipost.api.client.representations;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.MessageStatus.NOT_COMPLETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

public class MessageDeliveryTest {

    @Test
    public void deliveryWithNoDocumentsYieldsEmptyListWithDocuments() {
        MessageDelivery delivery = new MessageDelivery(null, DIGIPOST, NOT_COMPLETE, null);
        assertThat(delivery.getAllDocuments().collect(toList()), empty());
        assertThat(delivery.getAttachments(), empty());
    }

    @Test
    public void gettingAllDocumentsYieldsListWithPrimaryDocumentFirstFollowedByAttachments() {
        Document primary = new Document(UUID.randomUUID(), "primary", PDF);
        Document att1 = new Document(UUID.randomUUID(), "att1", PDF);
        Document att2 = new Document(UUID.randomUUID(), "att2", PDF);

        MessageDelivery delivery = new MessageDelivery(null, DIGIPOST, NOT_COMPLETE, null);
        delivery.primaryDocument = primary;
        delivery.attachments = asList(att1, att2);

        assertThat(delivery.getAllDocuments().collect(toList()), contains(primary, att1, att2));
        assertThat(delivery.getAttachments(), contains(att1, att2));
    }

    @Test
    public void findingDocumentsByUuid() {
        Document primary = new Document(UUID.randomUUID(), "primary", PDF);
        Document att1 = new Document(UUID.randomUUID(), "att1", PDF);

        MessageDelivery delivery = new MessageDelivery(null, DIGIPOST, NOT_COMPLETE, null);
        delivery.primaryDocument = primary;
        delivery.attachments = asList(att1);

        assertThat(delivery.getDocument(primary.uuid), is(primary));
        assertThat(delivery.getDocument(att1.uuid), is(att1));

        try {
            delivery.getDocument(UUID.randomUUID());
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("not found"));
            return;
        }
        fail("should throw exception");
    }
}
