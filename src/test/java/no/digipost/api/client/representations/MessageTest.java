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
package no.digipost.api.client.representations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.Document.technicalAttachment;
import static no.digipost.api.client.representations.FileType.*;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static no.motif.Iterate.on;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class MessageTest {

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldBeDirectPrintWhenMessageContainsOnlyPrintDetails() {
		Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF))
				.recipient(new MessageRecipient(new PrintDetails()))
				.build();
		assertTrue(message.isDirectPrint());
	}

	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsDigipostAddress() {
		Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
				.digipostAddress(new DigipostAddress("test.testson#1234"))
				.build();
		assertFalse(message.isDirectPrint());
	}
	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsNameAndAddress() {
		Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
				.recipient(new MessageRecipient(new NameAndAddress()))
				.build();
		assertFalse(message.isDirectPrint());
	}
	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsPersonalIdendificationNumber() {
		Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
				.personalIdentificationNumber(new PersonalIdentificationNumber("12125412435"))
				.build();
		assertFalse(message.isDirectPrint());
	}

	@Test
    public void possibleToPassNullForNoAttachments() {
		Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF))
				.digipostAddress(new DigipostAddress("test.testson#1234"))
				.build();
		assertThat(message.attachments, hasSize(0));
    }

	@Test
    public void changingThePassedAttachmentListDoesNotChangeTheMessage() {
		List<Document> attachments = new ArrayList<Document>(asList(new Document(UUID.randomUUID().toString(), "subject", PDF), new Document(UUID.randomUUID().toString(), "subject", PDF)));
		Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF))
				.digipostAddress(new DigipostAddress("test.testson#1234"))
				.attachments(attachments)
				.build();
		attachments.clear();

		assertThat(attachments, empty());
		assertThat(message.attachments, hasSize(2));
    }

	@Test
	public void sortsDocumentsByTheSameOrderAsTheyAppearInTheMessage() {
		Document hoved = new Document(UUID.randomUUID().toString(), "hoved", GIF);
		Document a1 = new Document(UUID.randomUUID().toString(), "a1", PDF);
		Document a2 = technicalAttachment("uhu, så teknisk!", ZIP);
		Document a3 = new Document(UUID.randomUUID().toString(), "a3", HTML);
		Message message = newMessage("id", hoved).attachments(asList(a1, a2, a3)).digipostAddress(new DigipostAddress("blah#ABCD")).build();

		assertThat(on(a2, hoved, a3, a1).sorted(message.documentOrder()), contains(hoved, a1, a2, a3));
	}

	@Test
	public void sortingDocumentsNotInMessageByOrderInMessageThrowsException() {
		Document hoved = new Document(UUID.randomUUID().toString(), "hoved", GIF);
		Document a1 = new Document(UUID.randomUUID().toString(), "a1", PDF);
		Document a2 = technicalAttachment("uhu, så teknisk!", ZIP);
		Document notInMessage = new Document(UUID.randomUUID().toString(), "a3", HTML);
		Message message = newMessage("id", hoved).attachments(asList(a1, a2)).digipostAddress(new DigipostAddress("blah#ABCD")).build();

		expectedException.expect(Message.CannotSortDocumentsUsingMessageOrder.class);
		expectedException.expectMessage("ikke sortere Document med uuid '" + notInMessage.uuid);
		Collections.sort(asList(a2, hoved, notInMessage, a1), message.documentOrder());
	}
}
