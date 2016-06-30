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
import no.digipost.api.client.delivery.DocumentContent;
import no.digipost.api.client.delivery.MessageDeliverer;
import no.digipost.api.client.delivery.OngoingDelivery.SendableForPrintOnly;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.util.DigipostApiMock;
import no.digipost.api.client.util.FakeEncryptionKey;
import no.digipost.api.client.util.JAXBContextUtils;
import no.digipost.api.client.util.MockfriendlyResponse;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static java.util.Arrays.asList;
import static no.digipost.api.client.DigipostClientConfig.DigipostClientConfigBuilder.newBuilder;
import static no.digipost.api.client.delivery.ApiFlavor.ATOMIC_REST;
import static no.digipost.api.client.delivery.ApiFlavor.STEPWISE_REST;
import static no.digipost.api.client.pdf.EksempelPdf.*;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
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
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
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
	private CloseableHttpResponse mockClientResponse;

	@Mock
	private CloseableHttpResponse mockClientResponse2;

	@Mock
	private ApiService api;

	@Mock
	IdentificationResultWithEncryptionKey identificationResultWithEncryptionKey;

	private MockfriendlyResponse encryptionKeyResponse;

	@Spy
	private PdfValidator pdfValidator;

	private MessageSender sender;
	private MessageSender cachelessSender;
	private EncryptionKey fakeEncryptionKey;

	@Before
	public void setup() {
		this.fakeEncryptionKey = FakeEncryptionKey.createFakeEncryptionKey();
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.encryptionKeyContext, fakeEncryptionKey, bao);

		encryptionKeyResponse = MockfriendlyResponse.MockedResponseBuilder.create()
				.status(SC_OK)
				.entity(new ByteArrayEntity(bao.toByteArray()))
				.build();

		sender = new MessageSender(newBuilder().cachePrintKey(true).build(), api, DigipostClient.NOOP_EVENT_LOGGER, pdfValidator);

		cachelessSender = new MessageSender(newBuilder().cachePrintKey(false).build(), api, DigipostClient.NOOP_EVENT_LOGGER, pdfValidator);
	}


	@Test
	public void skalHenteEksisterendeForsendelseHvisDenFinnesFraForr() {
		Message forsendelseIn = lagDefaultForsendelse();

		when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(SC_CONFLICT));
		when(mockClientResponse.getFirstHeader(anyString())).thenReturn(new BasicHeader("head", "er"));

		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.messageId, Channel.DIGIPOST, MessageStatus.NOT_COMPLETE, null);

		when(mockClientResponse2.getStatusLine()).thenReturn(new StatusLineMock(SC_OK));

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.messageDeliveryContext, eksisterendeForsendelse, bao);
		HttpEntity forsendelse = new ByteArrayEntity(bao.toByteArray());

		when(mockClientResponse2.getEntity()).thenReturn(forsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

		MessageDelivery delivery = sender.createOrFetchMessage(forsendelseIn);
		then(api).should().fetchExistingMessage(any(URI.class));

		assertTrue(delivery.isSameMessageAs(forsendelseIn));
	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevert() {
		Message forsendelseIn = lagDefaultForsendelse();

		when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(SC_CONFLICT));
		when(mockClientResponse.getFirstHeader(anyString())).thenReturn(new BasicHeader("head", "er"));
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.messageId, Channel.DIGIPOST, DELIVERED, now());
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.messageDeliveryContext, eksisterendeForsendelse, bao);

		when(mockClientResponse2.getStatusLine()).thenReturn(new StatusLineMock(SC_OK));
		when(mockClientResponse2.getEntity()).thenReturn(new ByteArrayEntity(bao.toByteArray()));
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

		when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(SC_CONFLICT));
		when(mockClientResponse.getFirstHeader(anyString())).thenReturn(new BasicHeader("head", "er"));
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.messageId, PRINT, DELIVERED_TO_PRINT, now());
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.messageDeliveryContext, eksisterendeForsendelse, bao);

		when(mockClientResponse2.getStatusLine()).thenReturn(new StatusLineMock(SC_OK));
		when(mockClientResponse2.getEntity()).thenReturn(new ByteArrayEntity(bao.toByteArray()));
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
	public void skal_ikke_bruke_cached_print_encryption_key_da_encryption_er_avskrudd() {
		when(api.getEncryptionKeyForPrint()).thenReturn(encryptionKeyResponse);

		cachelessSender.getEncryptionKeyForPrint();
		then(api).should(times(1)).getEncryptionKeyForPrint();

		cachelessSender.getEncryptionKeyForPrint();
		then(api).should(times(2)).getEncryptionKeyForPrint();

		DateTimeUtils.setCurrentMillisOffset(Duration.standardMinutes(10).getMillis());
		cachelessSender.getEncryptionKeyForPrint();
		then(api).should(times(3)).getEncryptionKeyForPrint();
	}


	@Test
	public void fallback_to_print_changes_filetype_html_to_pdf() {
		IdentificationResultWithEncryptionKey identificationResultWithEncryptionKey =
				new IdentificationResultWithEncryptionKey(IdentificationResult.digipost("123"), fakeEncryptionKey);

		when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(200));

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.identificationResultWithEncryptionKeyContext, identificationResultWithEncryptionKey, bao);

		when(mockClientResponse.getEntity()).thenReturn(new ByteArrayEntity(bao.toByteArray()));

		SenderInformation senderInformation = Mockito.mock(SenderInformation.class);
		when(senderInformation.getPdfValidationSettings()).thenReturn(new PdfValidationSettings(false, false, true, false));

		when(api.getEncryptionKey(any(URI.class))).thenReturn(encryptionKeyResponse);
		when(api.getEncryptionKeyForPrint()).thenReturn(encryptionKeyResponse);
		when(api.identifyAndGetEncryptionKey(any(Identification.class))).thenReturn(mockClientResponse);
		when(api.getSenderInformation(any(Message.class))).thenReturn(senderInformation);

		CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
		ByteArrayOutputStream bao2 = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.messageDeliveryContext,
				new MessageDelivery(UUID.randomUUID().toString(), Channel.PRINT, MessageStatus.COMPLETE, DateTime.now()), bao2);

		when(response.getEntity()).thenReturn(new ByteArrayEntity(bao2.toByteArray()));
		when(response.getStatusLine()).thenReturn(new StatusLineMock(200));

		when(api.multipartMessage(any(HttpEntity.class))).thenReturn(response);

		String messageId = UUID.randomUUID().toString();
		final Document printDocument = new Document(UUID.randomUUID().toString(), "subject", FileType.HTML).setPreEncrypt();
		final List<Document> printAttachments = asList(new Document(UUID.randomUUID().toString(), "attachment", FileType.HTML).setPreEncrypt());
		PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
		PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

		Map<String, DocumentContent> documentAndContent = new LinkedHashMap<>();

		MessageSender messageSender = new MessageSender(newBuilder().build(), api, DigipostClient.NOOP_EVENT_LOGGER, pdfValidator);
		Message message = newMessage(messageId, printDocument).attachments(printAttachments)
				.recipient(new MessageRecipient(new DigipostAddress("asdfasd"), new PrintDetails(recipient, returnAddress, A))).build();

		documentAndContent.put(message.primaryDocument.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf1Page()));
		for (Document attachment : printAttachments) {
			documentAndContent.put(attachment.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf1Page()));
		}

		messageSender.sendMultipartMessage(message, documentAndContent);
	}

	@Test
	public void setDigipostContentToUUIDTest(){
		Document printDocument = new Document(UUID.randomUUID().toString(), "subject", FileType.HTML).setPreEncrypt();
		Map<String, DocumentContent> documentAndContent = new LinkedHashMap<>();
		PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
		PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

		List<Document> printAttachments = asList(new Document(UUID.randomUUID().toString(), "attachment", FileType.HTML).setPreEncrypt());
		Message message = newMessage(UUID.randomUUID().toString(), printDocument).attachments(printAttachments)
				.recipient(new MessageRecipient(new DigipostAddress("asdfasd"), new PrintDetails(recipient, returnAddress, A))).build();

		documentAndContent.put(message.primaryDocument.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
		for (Document attachment : printAttachments) {
			documentAndContent.put(attachment.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
		}

		Map<Document, InputStream> documentAndInputStreams = new HashMap<>();
		Message digipostCopyMessage = Message.copyMessageWithOnlyDigipostDetails(message);
		MessageSender.setDigipostContentToUUID(documentAndContent, documentAndInputStreams, digipostCopyMessage.getAllDocuments());

		for(Document doc : digipostCopyMessage.getAllDocuments()){
			InputStream inputStream = documentAndInputStreams.get(doc);
			assertThat(inputStream, is(documentAndContent.get(doc.uuid).getDigipostContent()));
		}

		assertThat(digipostCopyMessage.recipient.hasPrintDetails(), is(false));
		assertThat(digipostCopyMessage.recipient.hasDigipostIdentification(),is(true));
	}

	@Test
	public void setPrintContentToUUIDTest(){
		Document printDocument = new Document(UUID.randomUUID().toString(), "subject", FileType.HTML).setPreEncrypt();
		Map<String, DocumentContent> documentAndContent = new LinkedHashMap<>();
		PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
		PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

		List<Document> printAttachments = asList(new Document(UUID.randomUUID().toString(), "attachment", FileType.HTML).setPreEncrypt());
		Message message = newMessage(UUID.randomUUID().toString(), printDocument).attachments(printAttachments)
				.recipient(new MessageRecipient(new DigipostAddress("asdfasd"), new PrintDetails(recipient, returnAddress, A))).build();

		documentAndContent.put(message.primaryDocument.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
		for (Document attachment : printAttachments) {
			documentAndContent.put(attachment.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
		}

		Map<Document, InputStream> documentAndInputStreams = new HashMap<>();
		Message printCopyMessage = Message.copyMessageWithOnlyPrintDetails(message);
		MessageSender.setPrintContentToUUID(documentAndContent, documentAndInputStreams, printCopyMessage.getAllDocuments());

		for(Document doc : printCopyMessage.getAllDocuments()){
			InputStream inputStream = documentAndInputStreams.get(doc);
			assertThat(inputStream, is(documentAndContent.get(doc.uuid).getPrintContent()));
		}

		assertThat(printCopyMessage.recipient.hasPrintDetails(), is(true));
		assertThat(printCopyMessage.recipient.hasDigipostIdentification(),is(false));
	}

	@Test
	public void passes_pdf_validation_for_printonly_message() throws IOException {
		String messageId = UUID.randomUUID().toString();
		when(api.getEncryptionKey(any(URI.class))).thenReturn(encryptionKeyResponse);
		when(api.getEncryptionKeyForPrint()).thenReturn(encryptionKeyResponse);
		when(api.createMessage(any(Message.class))).thenReturn(mockClientResponse);
		when(api.addContent(any(Document.class), any(InputStream.class))).thenReturn(mockClientResponse);
		when(api.multipartMessage(any(HttpEntity.class))).thenReturn(mockClientResponse);
		when(api.send(any(MessageDelivery.class))).thenReturn(mockClientResponse);
		when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(SC_OK));

		final Document printDocument = new Document(UUID.randomUUID().toString(), "subject", FileType.PDF).setPreEncrypt();
		final List<Document> printAttachments = asList(new Document(UUID.randomUUID().toString(), "attachment", FileType.PDF).setPreEncrypt());

		MessageDelivery incompleteDelivery = MessageDeliverySetter.setMessageDeliveryStatus(new MessageDelivery(messageId, PRINT, NOT_COMPLETE, now()), printDocument,
				printAttachments, new Link(SEND, new DigipostUri("/send")));

		final List<Document> allDocuments = the(printDocument).append(printAttachments).collect();

		for (Document document : allDocuments) {
			document.addLink(new Link(GET_ENCRYPTION_KEY, new DigipostUri("/encrypt")));
			document.setPreEncrypt();
		}


		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.messageDeliveryContext, incompleteDelivery, bao);
		byte[] bytes = bao.toByteArray();
		ByteArrayOutputStream bao2 = new ByteArrayOutputStream();
		JAXBContextUtils.marshal(JAXBContextUtils.messageDeliveryContext,
				new MessageDelivery(messageId, PRINT, DELIVERED_TO_PRINT, now()), bao2);

		when(mockClientResponse.getEntity())
				.thenReturn(new ByteArrayEntity(bytes), new ByteArrayEntity(bytes), new ByteArrayEntity(bytes))
				.thenReturn(new ByteArrayEntity(bao2.toByteArray()));


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

	public static class StatusLineMock implements StatusLine {

		private final int statusCode;
		public StatusLineMock(int statusCode){
			this.statusCode = statusCode;
		}

		@Override
		public ProtocolVersion getProtocolVersion() {
			return null;
		}

		@Override
		public int getStatusCode() {
			return statusCode;
		}

		@Override
		public String getReasonPhrase() {
			return null;
		}
	}
}
