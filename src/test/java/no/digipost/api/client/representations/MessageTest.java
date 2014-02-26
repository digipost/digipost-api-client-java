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

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;


public class MessageTest {

	@Test
	public void shouldBeDirectPrintWhenMessageContainsOnlyPrintDetails() {
		Message message = new Message(UUID.randomUUID().toString(), new MessageRecipient(new PrintDetails()),
				new Document(), new ArrayList<Document>());
		assertTrue(message.isDirectPrint());
	}

	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsDigipostAddress() {
		Message message = new Message(UUID.randomUUID().toString(), new DigipostAddress("test.testson#1234"),
				new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), PASSWORD, NORMAL),
				new ArrayList<Document>());
		assertFalse(message.isDirectPrint());
	}
	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsNameAndAddress() {
		Message message = new Message(UUID.randomUUID().toString(), new MessageRecipient(new NameAndAddress()),
				new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), PASSWORD, NORMAL),
				new ArrayList<Document>());
		assertFalse(message.isDirectPrint());
	}
	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsPersonalIdendificationNumber() {
		Message message = new Message(UUID.randomUUID().toString(), new PersonalIdentificationNumber("12125412435"),
				new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), PASSWORD, NORMAL),
				new ArrayList<Document>());
		assertFalse(message.isDirectPrint());
	}

	@Test
    public void possibleToPassNullForNoAttachments() {
	    Message message = new Message(UUID.randomUUID().toString(), new DigipostAddress("test.testson#1234"), new Document(UUID.randomUUID().toString(), "subject", PDF), null);
		assertThat(message.attachments, hasSize(0));
    }

	@Test
    public void changingThePassedAttachmentListDoesNotChangeTheMessage() {
		List<Document> attachments = new ArrayList<Document>(asList(new Document(UUID.randomUUID().toString(), "subject", PDF), new Document(UUID.randomUUID().toString(), "subject", PDF)));
		Message message = new Message(UUID.randomUUID().toString(), new DigipostAddress("test.testson#1234"), new Document(UUID.randomUUID().toString(), "subject", PDF), attachments);
		attachments.clear();

		assertThat(attachments, empty());
		assertThat(message.getAttachments(), hasSize(2));
    }
}
