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
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.AuthenticationLevel.*;
import static no.digipost.api.client.representations.DocumentEventType.*;
import static no.digipost.api.client.representations.ErrorType.CLIENT_DATA;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.PrintDetails.PostType.B;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class XsdValidationTest {


    private static final Logger LOG = LoggerFactory.getLogger(XsdValidationTest.class);
    private final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    private Schema schema;
    private JAXBContext jaxbContext;

	private Link link;


	@Before
	public void setUp() throws SAXException, JAXBException {
		schema = schemaFactory.newSchema(getClass().getResource("/xsd/api_v6.xsd"));
		jaxbContext = JAXBContext.newInstance("no.digipost.api.client.representations");

		link = new Link(Relation.SELF, new DigipostUri("http://localhost/self"), MediaTypes.DIGIPOST_MEDIA_TYPE_V6);
	}

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
				.standardNotification(StandardNotification.NONE)
				.build();


		Message unmarshalled = marshallValidateAndUnmarshall(messageWithDigipostAddress);
		assertThat("Does not include standard-notification element in XML when not set", unmarshalled.standardNotification, nullValue());

		marshallValidateAndUnmarshall(messageWithPersonalIdentificationNumber);
		marshallValidateAndUnmarshall(messageWithPreEncryptAndSenderId);

		unmarshalled = marshallValidateAndUnmarshall(messageWithTechnicalAttachment);
		assertThat(unmarshalled.standardNotification, is(StandardNotification.NONE));
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

		DocumentEvent movedFilesEvent = new DocumentEvent(UUID.randomUUID().toString(), MOVE_FILES_FROM_PUBLIC_SECTOR, DateTime.now(),
				new MoveFilesFromPublicSectorMetadata(true, DateTime.now().minusDays(3), "Subject", NORMAL, IDPORTEN_3, "fake-cert",
						"dest-mailbox", "dest-mailbox-address", asList(new DocumentMetadata(UUID.randomUUID().toString(), null)))
		);

		DocumentEvents documentEvents = new DocumentEvents(asList(openedEvent, failedEmailNotificationEvent, failedSmsNotificationEvent, movedFilesEvent));
		marshallValidateAndUnmarshall(documentEvents);
	}

	@Test
    public void validateMessageDelivery() {
		DateTime deliveryTime = DateTime.now();
		MessageDelivery delivery = new MessageDelivery(UUID.randomUUID().toString(), DeliveryMethod.DIGIPOST, MessageStatus.DELIVERED, deliveryTime);
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

	public <T> T marshallValidateAndUnmarshall(T element) {
		return marshallValidateAndUnmarshall(element, false);
	}

	public <T> T marshallValidateAndUnmarshall(T element, boolean log) {
		InputStream in = null;
		try (ByteArrayOutputStream resultXml = new ByteArrayOutputStream()) {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setSchema(schema);
			marshaller.setProperty("jaxb.formatted.output", true);
			marshaller.marshal(element, new StreamResult(resultXml));
			resultXml.flush();
			byte[] xml = resultXml.toByteArray();
			if (log) {
				LOG.info("Marshalled XML:\n{}", new String(xml));
			}
			in = new ByteArrayInputStream(xml);

			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(schema);
			@SuppressWarnings("unchecked")
            T unmarshalled = (T) unmarshaller.unmarshal(in);
			return unmarshalled;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			closeQuietly(in);
		}
	}
}