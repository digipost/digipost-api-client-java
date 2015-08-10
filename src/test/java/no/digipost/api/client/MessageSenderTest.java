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

import no.digipost.api.client.delivery.ApiFlavor;
import no.digipost.api.client.delivery.MessageDeliverer;
import no.digipost.api.client.delivery.OngoingDelivery.SendableForPrintOnly;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.util.MockfriendlyResponse;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.OK;
import static no.digipost.api.client.delivery.ApiFlavor.ATOMIC_REST;
import static no.digipost.api.client.delivery.ApiFlavor.STEPWISE_REST;
import static no.digipost.api.client.pdf.EksempelPdf.pdf20Pages;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf1Page;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.MessageStatus.*;
import static no.digipost.api.client.representations.PrintDetails.PostType.A;
import static no.digipost.api.client.representations.Relation.GET_ENCRYPTION_KEY;
import static no.digipost.api.client.representations.Relation.SEND;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static no.digipost.api.client.representations.sender.SenderFeature.*;
import static no.digipost.api.client.representations.sender.SenderStatus.VALID_SENDER;
import static no.motif.Singular.the;
import static org.hamcrest.Matchers.is;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageSenderTest {

	private static final Logger LOG = LoggerFactory.getLogger(MessageSenderTest.class);

	static {
		CryptoUtil.verifyJCE();
	}

	@Mock
	private Response mockClientResponse;

	@Mock
	private Response mockClientResponse2;

	@Mock
	private ApiService api;

	private MockfriendlyResponse encryptionKeyResponse;

	@Spy
	private PdfValidator pdfValidator;

	private MessageSender sender;

	@Before
	public void setup() {
		encryptionKeyResponse = MockfriendlyResponse.MockedResponseBuilder.create()
				.status(OK.getStatusCode())
				.entity(ApiServiceMock.createFakeEncryptionKey())
				.build();

		sender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER, pdfValidator);
	}


	@Test
	public void skalHenteEksisterendeForsendelseHvisDenFinnesFraForr() {
		Message forsendelseIn = lagDefaultForsendelse();

		when(mockClientResponse.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.messageId, Channel.DIGIPOST, MessageStatus.NOT_COMPLETE, null);

		when(mockClientResponse2.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
		when(mockClientResponse2.readEntity(MessageDelivery.class)).thenReturn(eksisterendeForsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

		MessageDelivery delivery = sender.createOrFetchMessage(forsendelseIn);
		then(api).should().fetchExistingMessage(any(URI.class));

		assertTrue(delivery.isSameMessageAs(forsendelseIn));
	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevert() {
		Message forsendelseIn = lagDefaultForsendelse();

		when(mockClientResponse.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.messageId, DIGIPOST, DELIVERED, now());

		when(mockClientResponse2.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
		when(mockClientResponse2.readEntity(MessageDelivery.class)).thenReturn(eksisterendeForsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

		try {
			sender.createOrFetchMessage(forsendelseIn);
			fail();
		} catch (DigipostClientException e) {
			assertEquals(ErrorCode.DIGIPOST_MESSAGE_ALREADY_DELIVERED, e.getErrorCode());
		}

	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevertTilPrint() {
		Message forsendelseIn = lagDefaultForsendelse();

		when(mockClientResponse.getStatus()).thenReturn(CONFLICT.getStatusCode());
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.messageId, PRINT, DELIVERED_TO_PRINT, now());

		when(mockClientResponse2.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
		when(mockClientResponse2.readEntity(MessageDelivery.class)).thenReturn(eksisterendeForsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

		try {
			sender.createOrFetchMessage(forsendelseIn);
			fail();
		} catch (DigipostClientException e) {
			assertEquals(ErrorCode.PRINT_MESSAGE_ALREADY_DELIVERED, e.getErrorCode());
		}

	}

	@Test
	public void skal_bruke_cached_print_encryption_key() {
		when(api.getEncryptionKeyForPrint()).thenReturn(encryptionKeyResponse);

		sender.getEncryptionKeyForPrint();
		then(api).should(times(1)).getEncryptionKeyForPrint();

		sender.getEncryptionKeyForPrint();
		then(api).should(times(1)).getEncryptionKeyForPrint();

		DateTimeUtils.setCurrentMillisOffset(Duration.standardMinutes(10).getMillis());
		sender.getEncryptionKeyForPrint();
		then(api).should(times(2)).getEncryptionKeyForPrint();
	}

	@Test
	public void passes_pdf_validation_for_printonly_message() {

		String messageId = UUID.randomUUID().toString();
		when(api.getEncryptionKey(any(URI.class))).thenReturn(encryptionKeyResponse);
		when(api.getEncryptionKeyForPrint()).thenReturn(encryptionKeyResponse);
		when(api.createMessage(any(Message.class))).thenReturn(mockClientResponse);
		when(api.addContent(any(Document.class), any(InputStream.class))).thenReturn(mockClientResponse);
		when(api.multipartMessage(any(MultiPart.class))).thenReturn(mockClientResponse);
		when(api.send(any(MessageDelivery.class))).thenReturn(mockClientResponse);
		when(mockClientResponse.getStatus()).thenReturn(Status.OK.getStatusCode());

		final Document printDocument = new Document(UUID.randomUUID().toString(), "subject", FileType.PDF).setPreEncrypt();
		final List<Document> printAttachments = asList(new Document(UUID.randomUUID().toString(), "attachment", FileType.PDF).setPreEncrypt());
		MessageDelivery incompleteDelivery = new MessageDelivery(messageId, PRINT, NOT_COMPLETE, now()) {{
			primaryDocument = printDocument;
			attachments = printAttachments;
			addLink(new Link(SEND, new DigipostUri("/send")));
		}};
		final List<Document> allDocuments = the(printDocument).append(printAttachments).collect();

		for (Document document : allDocuments) {
			document.addLink(new Link(GET_ENCRYPTION_KEY, new DigipostUri("/encrypt")));
			document.setPreEncrypt();
		}

		when(mockClientResponse.readEntity(MessageDelivery.class))
			.thenReturn(incompleteDelivery, incompleteDelivery, incompleteDelivery)
			.thenReturn(new MessageDelivery(messageId, PRINT, DELIVERED_TO_PRINT, now()));

		PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
		PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

		when(api.getSenderInformation(any(MayHaveSender.class))).thenReturn(new SenderInformation(1337L, VALID_SENDER,
				asList(DIGIPOST_DELIVERY, DELIVERY_DIRECT_TO_PRINT, DELIVERY_DIRECT_TO_PRINT, PRINTVALIDATION_FONTS, PRINTVALIDATION_MARGINS_LEFT, PRINTVALIDATION_PDFVERSION)));

		for (ApiFlavor apiFlavor : asList(STEPWISE_REST, ATOMIC_REST)) {
			LOG.debug("Tester direkte til print med " + apiFlavor);
			MessageDeliverer deliverer = new MessageDeliverer(apiFlavor, sender);
    		Message message = newMessage(messageId, printDocument).attachments(printAttachments).printDetails(new PrintDetails(recipient, returnAddress, A)).build();

    		SendableForPrintOnly sendable = deliverer
    				.createPrintOnlyMessage(message)
    				.addContent(message.primaryDocument, pdf20Pages());
    		for (Document attachment : printAttachments) {
				sendable.addContent(attachment, printablePdf1Page());
			}
    		MessageDelivery delivery = sendable.send();
			assertThat(delivery.getStatus(), is(DELIVERED_TO_PRINT));
			then(pdfValidator).should(times(2)).validate(any(byte[].class), any(PdfValidationSettings.class));
    		reset(pdfValidator);
		}
	}

	private Message lagDefaultForsendelse() {
		return lagEnkeltForsendelse("emne", UUID.randomUUID().toString(), "12345678900");
	}

	private Message lagEnkeltForsendelse(final String subject, final String messageId, final String fnr) {
		return newMessage(messageId, new Document(UUID.randomUUID().toString(), subject, FileType.PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
				.personalIdentificationNumber(new PersonalIdentificationNumber(fnr))
				.build();
	}





	@After
	public void resetToSystemClock() {
		DateTimeUtils.setCurrentMillisSystem();
	}
}
