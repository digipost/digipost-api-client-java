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
package no.digipost.api.client.internal.delivery;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.delivery.MessageDeliveryApi;
import no.digipost.api.client.delivery.OngoingDelivery.SendableForPrintOnly;
import no.digipost.api.client.delivery.OngoingDelivery.SendableWithPrintFallback;
import no.digipost.api.client.internal.http.StatusLineMock;
import no.digipost.api.client.representations.Channel;
import no.digipost.api.client.representations.DigipostAddress;
import no.digipost.api.client.representations.DigipostUri;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.EncryptionCertificate;
import no.digipost.api.client.representations.EncryptionKey;
import no.digipost.api.client.representations.FileType;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.IdentificationResultWithEncryptionKey;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.MessageRecipient;
import no.digipost.api.client.representations.MessageStatus;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.PrintDetails;
import no.digipost.api.client.representations.PrintRecipient;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.security.FakeEncryptionKey;
import no.digipost.api.client.security.FakeEncryptionX509Certificate;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import no.digipost.sanitizing.HtmlValidator;
import no.digipost.time.ControllableClock;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.stream.Stream.concat;
import static no.digipost.api.client.DigipostClientConfig.newConfiguration;
import static no.digipost.api.client.pdf.EksempelPdf.pdf20Pages;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf1Page;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf2Pages;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.MessageStatus.DELIVERED_TO_PRINT;
import static no.digipost.api.client.representations.Relation.GET_ENCRYPTION_KEY;
import static no.digipost.api.client.representations.sender.SenderFeatureName.DELIVERY_DIRECT_TO_PRINT;
import static no.digipost.api.client.representations.sender.SenderFeatureName.DIGIPOST_DELIVERY;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_FONTS;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_MARGINS_LEFT;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_PDFVERSION;
import static no.digipost.api.client.representations.sender.SenderStatus.VALID_SENDER;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageDelivererTest {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDelivererTest.class);

    static {
        CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();
    }

    @Mock
    private CloseableHttpResponse mockClientResponse;

    @Mock
    private CloseableHttpResponse mockClientResponse2;

    @Mock
    private MessageDeliveryApi api;

    @Mock
    private IdentificationResultWithEncryptionKey identificationResultWithEncryptionKey;

    private MockfriendlyResponse encryptionCertificateResponse;

    private final ControllableClock clock = ControllableClock.freezedAt(Instant.now());

    @Spy
    private PdfValidator pdfValidator;
    @Spy
    private HtmlValidator htmlValidator;

    private MessageDeliverer sender;
    private MessageDeliverer cachelessSender;
    private EncryptionKey fakeEncryptionKey = FakeEncryptionKey.createFakeEncryptionKey();
    private EncryptionCertificate fakeEncryptionCertificate = FakeEncryptionX509Certificate.createFakeEncryptionCertificate();

    @BeforeEach
    public void setup() {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        marshal(jaxbContext, fakeEncryptionCertificate, bao);

        encryptionCertificateResponse = MockfriendlyResponse.MockedResponseBuilder.create()
                .status(SC_OK)
                .entity(new ByteArrayEntity(bao.toByteArray()))
                .build();

        sender = new MessageDeliverer(newConfiguration().clock(clock).build(), api, new DocumentsPreparer(pdfValidator, htmlValidator));

        cachelessSender = new MessageDeliverer(newConfiguration().clock(clock).disablePrintKeyCache().build(), api, new DocumentsPreparer(pdfValidator, htmlValidator));
    }

    @Test
    public void skal_bruke_cached_print_encryption_key() {
        when(api.getEncryptionCertificateForPrint()).thenReturn(encryptionCertificateResponse);

        sender.getEncryptionCertificateForPrint();
        then(api).should(times(1)).getEncryptionCertificateForPrint();

        clock.timePasses(ofMinutes(5));
        sender.getEncryptionCertificateForPrint();
        then(api).should(times(1)).getEncryptionCertificateForPrint();

        clock.timePasses(ofMillis(1));
        sender.getEncryptionCertificateForPrint();
        then(api).should(times(2)).getEncryptionCertificateForPrint();
    }

    @Test
    public void skal_ikke_bruke_cached_print_encryption_key_da_encryption_er_avskrudd() {
        when(api.getEncryptionCertificateForPrint()).thenReturn(encryptionCertificateResponse);

        cachelessSender.getEncryptionCertificateForPrint();
        then(api).should(times(1)).getEncryptionCertificateForPrint();

        cachelessSender.getEncryptionCertificateForPrint();
        then(api).should(times(2)).getEncryptionCertificateForPrint();

        clock.timePasses(ofMinutes(10));
        cachelessSender.getEncryptionCertificateForPrint();
        then(api).should(times(3)).getEncryptionCertificateForPrint();
    }


    @Test
    public void fallback_to_print_changes_filetype_html_to_pdf() {
        IdentificationResultWithEncryptionKey identificationResultWithEncryptionKey =
                new IdentificationResultWithEncryptionKey(IdentificationResult.digipost("123"), fakeEncryptionKey);

        when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(200));

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        marshal(jaxbContext, identificationResultWithEncryptionKey, bao);

        when(mockClientResponse.getEntity()).thenReturn(new ByteArrayEntity(bao.toByteArray()));

        when(api.identifyAndGetEncryptionKey(any(Identification.class))).thenReturn(mockClientResponse);

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        ByteArrayOutputStream bao2 = new ByteArrayOutputStream();
        marshal(jaxbContext,
                new MessageDelivery(UUID.randomUUID().toString(), Channel.PRINT, MessageStatus.COMPLETE, now()), bao2);

        when(response.getEntity()).thenReturn(new ByteArrayEntity(bao2.toByteArray()));
        when(response.getStatusLine()).thenReturn(new StatusLineMock(200));

        when(api.sendMultipartMessage(any(HttpEntity.class))).thenReturn(response);

        final Document printDocument = new Document(UUID.randomUUID(), "subject", FileType.HTML);
        final List<Document> printAttachments = asList(new Document(UUID.randomUUID(), "attachment", FileType.HTML));
        PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
        PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

        Map<UUID, DocumentContent> documentAndContent = new LinkedHashMap<>();

        MessageDeliverer deliverer = new MessageDeliverer(newConfiguration().clock(clock).build(), api, new DocumentsPreparer(pdfValidator, htmlValidator));
        Message message = Message.newMessage(UUID.randomUUID(), printDocument).attachments(printAttachments)
                .recipient(new MessageRecipient(new DigipostAddress("asdfasd"), new PrintDetails(recipient, returnAddress))).build();

        documentAndContent.put(message.primaryDocument.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf1Page()));
        for (Document attachment : printAttachments) {
            documentAndContent.put(attachment.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf1Page()));
        }
        SendableWithPrintFallback delivery = deliverer.createMessage(message).addContent(printDocument, printablePdf1Page());
        printAttachments.forEach(d -> delivery.addContent(d, printablePdf1Page(), printablePdf1Page()));
        delivery.send();

    }

    @Test
    public void setDigipostContentToUUIDTest(){
        Document printDocument = new Document(UUID.randomUUID(), "subject", FileType.HTML).encrypt();
        Map<UUID, DocumentContent> documentAndContent = new LinkedHashMap<>();
        PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
        PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

        List<Document> printAttachments = asList(new Document(UUID.randomUUID(), "attachment", FileType.HTML).encrypt());
        Message message = Message.newMessage(UUID.randomUUID(), printDocument).attachments(printAttachments)
                .recipient(new MessageRecipient(new DigipostAddress("asdfasd"), new PrintDetails(recipient, returnAddress))).build();

        documentAndContent.put(message.primaryDocument.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
        for (Document attachment : printAttachments) {
            documentAndContent.put(attachment.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
        }

        Map<Document, InputStream> documentAndInputStreams = new HashMap<>();
        Message digipostCopyMessage = Message.copyMessageWithOnlyDigipostDetails(message);
        MessageDeliverer.setDigipostContentToUUID(documentAndContent, documentAndInputStreams, digipostCopyMessage.getAllDocuments());

        digipostCopyMessage.getAllDocuments().forEach(doc -> {
            InputStream inputStream = documentAndInputStreams.get(doc);
            assertThat(inputStream, is(documentAndContent.get(doc.uuid).getDigipostContent()));
        });

        assertThat(digipostCopyMessage.recipient.hasPrintDetails(), is(false));
        assertThat(digipostCopyMessage.recipient.hasDigipostIdentification(),is(true));
    }

    @Test
    public void setPrintContentToUUIDTest(){
        Document printDocument = new Document(UUID.randomUUID(), "subject", FileType.HTML).encrypt();
        Map<UUID, DocumentContent> documentAndContent = new LinkedHashMap<>();
        PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
        PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

        List<Document> printAttachments = asList(new Document(UUID.randomUUID(), "attachment", FileType.HTML).encrypt());
        Message message = Message.newMessage(UUID.randomUUID(), printDocument).attachments(printAttachments)
                .recipient(new MessageRecipient(new DigipostAddress("asdfasd"), new PrintDetails(recipient, returnAddress))).build();

        documentAndContent.put(message.primaryDocument.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
        for (Document attachment : printAttachments) {
            documentAndContent.put(attachment.uuid, DocumentContent.CreateMultiStreamContent(printablePdf1Page(), printablePdf2Pages()));
        }

        Map<Document, InputStream> documentAndInputStreams = new HashMap<>();
        Message printCopyMessage = Message.copyPrintMessage(message);
        MessageDeliverer.setPrintContentToUUID(documentAndContent, documentAndInputStreams, printCopyMessage.getAllDocuments());

        printCopyMessage.getAllDocuments().forEach(doc -> {
            InputStream inputStream = documentAndInputStreams.get(doc);
            assertThat(inputStream, is(documentAndContent.get(doc.uuid).getPrintContent()));
        });

        assertThat(printCopyMessage.recipient.hasPrintDetails(), is(true));
        assertThat(printCopyMessage.recipient.hasDigipostIdentification(),is(true));
    }

    @Test
    public void passes_pdf_validation_for_printonly_message() throws IOException {
        UUID messageId = UUID.randomUUID();
        when(api.getEncryptionCertificateForPrint()).thenReturn(encryptionCertificateResponse);
        when(api.sendMultipartMessage(any(HttpEntity.class))).thenReturn(mockClientResponse);
        when(mockClientResponse.getStatusLine()).thenReturn(new StatusLineMock(SC_OK));

        final Document printDocument = new Document(UUID.randomUUID(), "subject", FileType.PDF).encrypt();
        final List<Document> printAttachments = asList(new Document(UUID.randomUUID(), "attachment", FileType.PDF).encrypt());

        concat(Stream.of(printDocument), printAttachments.stream()).forEach(document -> document.addLink(new Link(GET_ENCRYPTION_KEY, new DigipostUri("/encrypt"))));

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        marshal(jaxbContext,
                new MessageDelivery(messageId.toString(), PRINT, DELIVERED_TO_PRINT, now()), bao);

        when(mockClientResponse.getEntity())
                .thenReturn(new ByteArrayEntity(bao.toByteArray()));


        PrintRecipient recipient = new PrintRecipient("Rallhild Ralleberg", new NorwegianAddress("0560", "Oslo"));
        PrintRecipient returnAddress = new PrintRecipient("Megacorp", new NorwegianAddress("0105", "Oslo"));

        when(api.getSenderInformation(any(MayHaveSender.class))).thenReturn(new SenderInformation(SenderId.of(1337), VALID_SENDER,
                asList(
                        DIGIPOST_DELIVERY.withNoParam(), DELIVERY_DIRECT_TO_PRINT.withNoParam(), DELIVERY_DIRECT_TO_PRINT.withNoParam(),
                        PRINTVALIDATION_FONTS.withNoParam(), PRINTVALIDATION_MARGINS_LEFT.withNoParam(), PRINTVALIDATION_PDFVERSION.withNoParam())
        ));

        LOG.debug("Tester direkte til print");
        Message message = Message.newMessage(messageId, printDocument).attachments(printAttachments).printDetails(new PrintDetails(recipient, returnAddress)).build();

        SendableForPrintOnly sendable = sender
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

    @ParameterizedTest(name = "{index} {0} clones as expected")
    @MethodSource("provideMessages")
    void setMapAndMessageToPrintClonesMessage(String description, Message message) {
        Map<UUID, DocumentContent> docs2ContentMap = Collections
                .singletonMap(message.primaryDocument.uuid, DocumentContent.CreateBothStreamContent(printablePdf1Page()));
        Map<Document, InputStream> docs2StreamMap = new HashMap<>();
        Message result = MessageDeliverer.setMapAndMessageToPrint(message, docs2ContentMap, docs2StreamMap);
        assertNotSame(result, message);
        assertMessagesAreEqual(message, result);
    }

    private static Stream<Arguments> provideMessages() {
        Document primaryDoc = new Document(
                UUID.randomUUID(), RandomStringUtils.randomAlphanumeric(1, 128), FileType.PDF
        );
        PrintRecipient printRecipient = new PrintRecipient();
        PrintDetails printDetails = new PrintDetails(printRecipient, printRecipient);
        MessageRecipient directPrintRecipient = new MessageRecipient(printDetails);
        MessageRecipient fallbackPrintRecipient = new MessageRecipient(
                new PersonalIdentificationNumber(RandomStringUtils.randomNumeric(11)), printDetails
        );
        return Stream.of(
                Arguments.of("direct print message", Message.newMessage(UUID.randomUUID(), primaryDoc)
                        .recipient(directPrintRecipient).build()),
                Arguments.of("fallback print message", Message.newMessage(UUID.randomUUID(), primaryDoc)
                        .recipient(fallbackPrintRecipient).build())
        );
    }

    private static void assertMessagesAreEqual(Message message, Message result) {
        assertEquals(message.attachments, result.attachments);
        assertEquals(message.batch, result.batch);
        assertEquals(message.deliveryTime, result.deliveryTime);
        assertEquals(message.invoiceReference, result.invoiceReference);
        assertEquals(message.messageId, result.messageId);
        assertEquals(message.primaryDocument.uuid, result.primaryDocument.uuid);
        assertEquals(message.primaryDocument.subject, result.primaryDocument.subject);
        assertEquals(message.primaryDocument.getDigipostFileType(), result.primaryDocument.getDigipostFileType());
        assertEquals(message.requestForRegistration, result.requestForRegistration);
        assertEquals(message.printIfUnread, result.printIfUnread);
        assertTrue(
                EqualsBuilder.reflectionEquals(message.recipient, result.recipient),
                String.format(
                        "expected%n%s but was: %n%s",
                        ToStringBuilder.reflectionToString(message.recipient),
                        ToStringBuilder.reflectionToString(result.recipient)
                )
        );
        assertEquals(message.senderId, result.senderId);
        assertEquals(message.senderOrganization, result.senderOrganization);
    }

}
