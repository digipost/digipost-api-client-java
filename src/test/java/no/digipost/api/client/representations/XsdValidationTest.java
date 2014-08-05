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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.AuthenticationLevel.TWO_FACTOR;
import static no.digipost.api.client.representations.ErrorType.CLIENT_DATA;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.PrintDetails.PostType.B;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

public class XsdValidationTest {

	private Marshaller marshaller;
	private Link link;

	@Before
	public void setUp() throws SAXException, JAXBException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(getClass().getResource("/xsd/api_v6.xsd"));
		marshaller = JAXBContext.newInstance("no.digipost.api.client.representations").createMarshaller();
		marshaller.setSchema(schema);

		link = new Link(Relation.SELF, new DigipostUri("http://localhost/self"), MediaTypes.DIGIPOST_MEDIA_TYPE_V6);
	}

	@Test
	public void validateRecipients() throws JAXBException {
		Address address = new Address("Streetn", "houseNumber", "houseLetter", "additionalAddressLine", "zipCode", "city");
		ArrayList<Address> addresses = new ArrayList<Address>();
		addresses.add(address);
		Recipients recipients = new Recipients();
		Recipient recipient = new Recipient("Even", "Emmil", "Beinlaus", "even.beinlaus#1234", addresses, link);
		recipients.add(recipient);
		marshallAndValidate(recipients);
	}

	@Test
	public void validateErrorMessage() throws JAXBException {
		marshallAndValidate(new ErrorMessage(CLIENT_DATA, "Error message", link));
	}

	@Test
	public void validateAutocomplete() throws JAXBException {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		suggestions.add(new Suggestion("even", link));
		marshallAndValidate(new Autocomplete(suggestions, link));
	}

	@Test
	public void validateEntryPoint() throws JAXBException {
		marshallAndValidate(new EntryPoint("dummy certificate-PEM", link, link, link));
	}

	@Test
	public void validateMessage() throws JAXBException {
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
		marshallAndValidate(messageWithDigipostAddress);

		Document primaryDocumentToPreEncrypt = new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL);
		Message messageWithPreEncryptAndSenderId = newMessage(UUID.randomUUID().toString(), primaryDocumentToPreEncrypt)
				.personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
				.senderId(10L)
				.build();

		primaryDocumentToPreEncrypt.setPreEncrypt();

		marshallAndValidate(messageWithDigipostAddress);
		marshallAndValidate(messageWithPersonalIdentificationNumber);
		marshallAndValidate(messageWithPreEncryptAndSenderId);
	}

	@Test
	public void validateMessage_invoicingAccount() throws JAXBException {
		Document document = new Document(UUID.randomUUID().toString(), "subject", PDF, null, null, null, TWO_FACTOR, NORMAL);
		Message message = newMessage(UUID.randomUUID().toString(), document)
				.digipostAddress(new DigipostAddress("even.beinlaus#1234"))
				.invoicingAccount("ACCOUNT01")
				.deliveryTime(DateTime.now())
				.build();
		marshallAndValidate(message);
	}

	@Test
	public void validatePrintMessage() throws JAXBException {
		PrintRecipient address = new PrintRecipient("name", new NorwegianAddress("1234", "Oslo"));
		Message message = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
				)
				.recipient(new MessageRecipient(new PrintDetails(address, address, B)))
				.build();
		marshallAndValidate(message);
	}

	@Test
	public void validatePrintMessageWithForeignRecipiantWihtCountry() throws JAXBException {
		PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", "Sverige", null));
		Message message = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
				)
				.recipient(new MessageRecipient(new PrintDetails(address, address, B)))
				.build();
		marshallAndValidate(message);
	}

	@Test
	public void validatePrintMessageWithForeignRecipiantWihtCountryCode() throws JAXBException {
		PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", null, "SE"));
		Message message = newMessage(UUID.randomUUID().toString(),
						new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
				)
				.recipient(new MessageRecipient(new PrintDetails(address, address, B)))
				.build();
		marshallAndValidate(message);
	}

	public void marshallAndValidate(final Object element) throws JAXBException {
		marshaller.marshal(element, new DefaultHandler());
	}
}