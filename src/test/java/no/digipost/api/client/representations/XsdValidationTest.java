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

import static no.digipost.api.client.representations.PrintDetails.PostType.B;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XsdValidationTest {

	private Marshaller marshaller;
	private Link link;

	@Before
	public void setUp() throws SAXException, JAXBException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(getClass().getResource("/xsd/api_v3.xsd"));
		marshaller = JAXBContext.newInstance("no.digipost.api.client.representations").createMarshaller();
		marshaller.setSchema(schema);

		link = new Link(Relation.SELF, new DigipostUri("http://localhost/self"), MediaTypes.DIGIPOST_MEDIA_TYPE_V3);
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
		marshallAndValidate(new ErrorMessage("Error message", link));
	}

	@Test
	public void validateAutocomplete() throws JAXBException {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		suggestions.add(new Suggestion("even", link));
		marshallAndValidate(new Autocomplete(suggestions, link));
	}

	@Test
	public void validateEntryPoint() throws JAXBException {
		marshallAndValidate(new EntryPoint(link, link, link));
	}

	@Test
	public void validateMessage() throws JAXBException {
		Message messageWithDigipostAddress = new Message("messageId", "subject", new DigipostAddress("even.beinlaus#1234"), new SmsNotification(0),
				AuthenticationLevel.TWO_FACTOR, SensitivityLevel.NORMAL);
		Message messageWithPersonalIdentificationNumber = new Message("messageId", "subject", new PersonalIdentificationNumber(
				"12345678901"), new SmsNotification(0), AuthenticationLevel.TWO_FACTOR, SensitivityLevel.NORMAL);
		marshallAndValidate(messageWithDigipostAddress);
		Message messageWithPreEncryptAndSenderId = new Message("messageId", "subject", new PersonalIdentificationNumber("12345678901"),
				new SmsNotification(0), AuthenticationLevel.TWO_FACTOR, SensitivityLevel.NORMAL);
		messageWithPreEncryptAndSenderId.setSenderId(10L);
		messageWithPreEncryptAndSenderId.setPreEncrypt();

		marshallAndValidate(messageWithDigipostAddress);
		marshallAndValidate(messageWithPersonalIdentificationNumber);
		marshallAndValidate(messageWithPreEncryptAndSenderId);
	}

	@Test
	public void validatePrintMessage() throws JAXBException {
		PrintRecipient address = new PrintRecipient("name", new NorwegianAddress("1234", "Oslo"));
		Message message = new Message("messageId", new PrintDetails(address, address, B));
		marshallAndValidate(message);
	}

	@Test
	public void validatePrintMessageWithForeignRecipiantWihtCountry() throws JAXBException {
		PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", "Sverige", null));
		Message message = new Message("messageId", new PrintDetails(address, address, B));
		marshallAndValidate(message);
	}

	@Test
	public void validatePrintMessageWithForeignRecipiantWihtCountryCode() throws JAXBException {
		PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", null, "SE"));
		Message message = new Message("messageId", new PrintDetails(address, address, B));
		marshallAndValidate(message);
	}

	public void marshallAndValidate(final Object element) throws JAXBException {
		marshaller.marshal(element, new DefaultHandler());
	}
}