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


import org.joda.time.DateTime;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.AuthenticationLevel.*;
import static no.digipost.api.client.representations.DocumentEventType.*;
import static no.digipost.api.client.representations.ErrorType.CLIENT_DATA;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.PrintDetails.PostType.B;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static no.digipost.api.client.representations.XmlTestHelper.marshallValidateAndUnmarshall;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class XsdValidationTest {


	private Link link = new Link(Relation.SELF, new DigipostUri("http://localhost/self"), MediaTypes.DIGIPOST_MEDIA_TYPE_V6);


	@Test
	public void validateRecipients() {
		Address address = new Address("Streetn", "houseNumber", "houseLetter", "additionalAddressLine", "zipCode", "city");
		ArrayList<Address> addresses = new ArrayList<Address>();
		addresses.add(address);
		Recipients recipients = new Recipients();
		Recipient recipient = new Recipient("Even", "Emmil", "Beinlaus", "even.beinlaus#1234", addresses, link);
		recipients.add(recipient);
		marshallValidateAndUnmarshall(recipients);
	}

	@Test
	public void validateErrorMessage() {
		marshallValidateAndUnmarshall(new ErrorMessage(CLIENT_DATA, "Error message", link));
	}

	@Test
	public void validateAutocomplete() {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		suggestions.add(new Suggestion("even", link));
		marshallValidateAndUnmarshall(new Autocomplete(suggestions, link));
	}

	@Test
	public void validateEntryPoint() {
		marshallValidateAndUnmarshall(new EntryPoint("dummy certificate-PEM", link, link, link));
	}

	@Test
	public void validateMessage() {
		Message messageWithDigipostAddress = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL)
				)
				.digipostAddress(new DigipostAddress("even.beinlaus#1234"))
				.senderOrganization(new SenderOrganization("1337", "R&D"))
				.build();

		Message messageWithPersonalIdentificationNumber = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL)
				)
				.personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
				.build();

		Document primaryDocumentToPreEncrypt = new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL);
		Message messageWithPreEncryptAndSenderId = newMessage(UUID.randomUUID().toString(), primaryDocumentToPreEncrypt)
				.personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
				.senderId(10L)
				.build();

		primaryDocumentToPreEncrypt.setPreEncrypt();

		Message messageWithTechnicalAttachment = newMessage(UUID.randomUUID().toString(),
					new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL))
				.personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
				.attachments(Collections.singleton(Document.technicalAttachment("tech-type", PDF)))
				.build();


		marshallValidateAndUnmarshall(messageWithDigipostAddress);
		marshallValidateAndUnmarshall(messageWithPersonalIdentificationNumber);
		marshallValidateAndUnmarshall(messageWithPreEncryptAndSenderId);
		marshallValidateAndUnmarshall(messageWithTechnicalAttachment);
	}

	@Test
	public void validateMessage_invoicingAccount() {
		Document document = new Document(UUID.randomUUID().toString(), "subject", PDF, null, null, null, TWO_FACTOR, NORMAL);
		Message message = newMessage(UUID.randomUUID().toString(), document)
				.digipostAddress(new DigipostAddress("even.beinlaus#1234"))
				.invoiceReference("ACCOUNT01")
				.deliveryTime(DateTime.now())
				.build();
		marshallValidateAndUnmarshall(message);
	}

	@Test
	public void validatePrintMessage() {
		PrintRecipient address = new PrintRecipient("name", new NorwegianAddress("1234", "Oslo"));
		Message message = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
				)
				.recipient(new MessageRecipient(new PrintDetails(address, address, B)))
				.build();
		marshallValidateAndUnmarshall(message);
	}

	@Test
	public void validatePrintMessageWithForeignRecipiantWihtCountry() {
		PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", "Sverige", null));
		Message message = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
				)
				.recipient(new MessageRecipient(new PrintDetails(address, address, B)))
				.build();
		marshallValidateAndUnmarshall(message);
	}

	@Test
	public void validatePrintMessageWithForeignRecipiantWihtCountryCode() {
		PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", null, "SE"));
		Message message = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
				)
				.recipient(new MessageRecipient(new PrintDetails(address, address, B)))
				.build();
		marshallValidateAndUnmarshall(message);
	}

	@Test
	public void validateDocumentEvents() {
		DocumentEvent openedEvent = new DocumentEvent(UUID.randomUUID().toString(), OPENED, DateTime.now());

		DocumentEvent failedEmailNotificationEvent = new DocumentEvent(UUID.randomUUID().toString(), EMAIL_NOTIFICATION_FAILED, DateTime.now(),
				new EmailNotificationFailedMetadata("emailAddress", "ERROR_CODE"));

		DocumentEvent failedSmsNotificationEvent = new DocumentEvent(UUID.randomUUID().toString(), SMS_NOTIFICATION_FAILED, DateTime.now(),
				new SmsNotificationFailedMetadata("12345678", "ERROR_CODE"));

		DocumentEvent printFailedEvent = new DocumentEvent(UUID.randomUUID().toString(), PRINT_FAILED, DateTime.now(),
				new FailedPrintMetadata("Feil dimensjoner"));

		DocumentEvent postmarkedEvent = new DocumentEvent(UUID.randomUUID().toString(), POSTMARKED, DateTime.now(),
				new PostmarkedMetadata(DateTime.now()));

		DocumentEvent movedFilesEvent = new DocumentEvent(UUID.randomUUID().toString(), MOVE_FILES_FROM_PUBLIC_SECTOR, DateTime.now(),
				new MoveFilesFromPublicSectorMetadata(true, DateTime.now().minusDays(3), "Subject", NORMAL, IDPORTEN_3, "fake-cert",
						"dest-mailbox", "dest-mailbox-address", asList(new DocumentMetadata(UUID.randomUUID().toString(), null)))
		);

		DocumentEvent shreddedEvent = new DocumentEvent(UUID.randomUUID().toString(), SHREDDED, DateTime.now());

		DocumentEvents documentEvents = new DocumentEvents(asList(openedEvent, failedEmailNotificationEvent,
				failedSmsNotificationEvent, printFailedEvent, movedFilesEvent, postmarkedEvent, shreddedEvent));
		marshallValidateAndUnmarshall(documentEvents);
	}

	@Test
	public void validate_document_status_simple() {
		DocumentStatus primaryDoc = new DocumentStatus(UUID.randomUUID().toString(), DeliveryStatus.NOT_DELIVERED, DateTime.now(), null, null, Channel.DIGIPOST, true,
				null, HashAlgorithm.NONE, null, null);
		marshallValidateAndUnmarshall(primaryDoc);
	}

	@Test
	public void validate_document_status_with_attachments() {
		DocumentStatus attachment1 = createDocumentStatus(false);
		DocumentStatus attachment2 = createDocumentStatus(false);
		DocumentStatus primaryDoc = createDocumentStatus(true, attachment1, attachment2);

		marshallValidateAndUnmarshall(primaryDoc);
	}

	private DocumentStatus createDocumentStatus(boolean isPrimaryDocument, DocumentStatus ... attachments) {
		return new DocumentStatus(UUID.randomUUID().toString(), DeliveryStatus.DELIVERED, DateTime.now(), DateTime.now(), Read.Y, Channel.PRINT, isPrimaryDocument,
				"asdf", HashAlgorithm.SHA256, Arrays.asList(attachments), null);
	}

	@Test
    public void validateMessageDelivery() {
		DateTime deliveryTime = DateTime.now();
		MessageDelivery delivery = new MessageDelivery(UUID.randomUUID().toString(), Channel.DIGIPOST, MessageStatus.DELIVERED, deliveryTime);
		delivery.primaryDocument = new Document(UUID.randomUUID().toString(), "primary", FileType.PDF);
		MessageDelivery unmarshalled = marshallValidateAndUnmarshall(delivery);
		assertThat(unmarshalled, not(sameInstance(delivery)));
		assertThat(unmarshalled.primaryDocument.uuid, is(delivery.primaryDocument.uuid));
		assertThat(unmarshalled.primaryDocument.subject, is(delivery.primaryDocument.subject));
		assertThat(unmarshalled.messageId, is(delivery.messageId));
		assertThat(unmarshalled.deliveryMethod, is(delivery.deliveryMethod));
		assertThat(unmarshalled.deliveryTime, is(delivery.deliveryTime));
		assertThat(unmarshalled.status, is(delivery.status));
    }

}