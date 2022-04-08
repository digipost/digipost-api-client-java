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

import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.html.EksempelHtml;
import no.digipost.api.client.representations.Channel;
import no.digipost.api.client.representations.DigipostAddress;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.FileType;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.Message.MessageBuilder;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PrintDetails;
import no.digipost.api.client.representations.PrintRecipient;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.security.DigipostPublicKey;
import no.digipost.api.client.security.Encrypter;
import no.digipost.api.client.security.FakeEncryptionKey;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import no.digipost.sanitizing.HtmlValidator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static co.unruly.matchers.Java8Matchers.where;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static no.digipost.api.client.pdf.EksempelPdf.pdf20Pages;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf1Page;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf2Pages;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.FileType.GIF;
import static no.digipost.api.client.representations.FileType.HTML;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.security.Encrypter.FAIL_IF_TRYING_TO_ENCRYPT;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public class DocumentsPreparerTest {

    private static final byte[] pdf20Pages;
    static {
        CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();
        try {
            pdf20Pages = toByteArray(pdf20Pages());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private final DocumentsPreparer preparer = new DocumentsPreparer(new PdfValidator(), new HtmlValidator());

    private final Encrypter encrypter = Encrypter.using(new DigipostPublicKey(FakeEncryptionKey.createFakeEncryptionKey()));

    private final Document primaryDocument = new Document(UUID.randomUUID(), "primary", PDF);
    private final Map<Document, InputStream> documents = new HashMap<Document, InputStream>() {{ put(primaryDocument, printablePdf1Page()); }};
    private final MessageBuilder messageBuilder = Message.newMessage(UUID.randomUUID(), primaryDocument).printDetails(
            new PrintDetails(new PrintRecipient("Joe Schmoe", new NorwegianAddress("7845", "Far away")), new PrintRecipient("Dolly Parton", new NorwegianAddress("8942", "Farther away"))));

    @Test
    public void failsIfMessageHasAnyDocumentsRequiringPreEncryptionAndNoEncryptionKeyIsSupplied() throws IOException {
        primaryDocument.encrypt();

        DigipostClientException thrown = assertThrows(DigipostClientException.class,
                () -> preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT, () -> PdfValidationSettings.CHECK_ALL, DigipostClientConfig.newConfiguration().build()));
        assertThat(thrown, where(Exception::getMessage, containsString("no encryption key")));
    }

    @Test
    public void cannotSendNonPdfFilesToPrint() throws IOException {
        addAttachment("funny animated gif", GIF, toInputStream("content doesn't matter", UTF_8)).encrypt();

        DigipostClientException thrown = assertThrows(DigipostClientException.class,
                () -> preparer.prepare(documents, messageBuilder.build(), encrypter, () -> mock(PdfValidationSettings.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS)), DigipostClientConfig.newConfiguration().build()));
        assertThat(thrown, where(Exception::getMessage, containsString("filetype gif")));
    }

    @Test
    public void deniesNonValidatingPdfForBothPrintAndWeb() {
        for (Channel deliveryMethod : Channel.values()) {
            try {
                preparer.validateAndSetNrOfPages(deliveryMethod, new Document(UUID.randomUUID(), null, PDF), new byte[]{65, 65, 65, 65}, () -> mock(PdfValidationSettings.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS)));
            } catch (DigipostClientException e) {
                assertThat(e.getMessage(), containsString("Could not parse"));
                continue;
            }
            fail("Should fail validation for bogus PDF using " + Channel.class.getSimpleName() + "." + deliveryMethod);
        }
    }

    @Test
    public void passesDocumentForWebWhichWouldNotBeOkForPrint() throws IOException {
        preparer.validateAndSetNrOfPages(DIGIPOST, new Document(UUID.randomUUID(), null, PDF), pdf20Pages, () -> PdfValidationSettings.CHECK_ALL);

        DigipostClientException thrown = assertThrows(DigipostClientException.class,
                () -> preparer.validateAndSetNrOfPages(PRINT, new Document(UUID.randomUUID(), null, PDF), pdf20Pages, () -> PdfValidationSettings.CHECK_ALL));
        assertThat(thrown, where(Exception::getMessage, containsString("too many pages")));
    }

    @Test
    public void passesHtmlDocumentDefaultConfig() throws IOException {
        final Document primary = new Document(UUID.randomUUID(), "primary", HTML);
        primary.encrypt();
        final Map<Document, InputStream> documents = new HashMap<Document, InputStream>() {{
            put(primary, EksempelHtml.validHtml());
        }};
        final MessageBuilder messageBuilder = Message.newMessage(UUID.randomUUID(), primaryDocument).recipient(new DigipostAddress("test.testson#1234"));

        final DigipostClientConfig config = DigipostClientConfig.newConfiguration().build();

        preparer.prepare(documents, messageBuilder.build(), encrypter, () -> PdfValidationSettings.CHECK_ALL, config);
    }

    @Test
    void failsDocumentWithFailOnSanitize() {
        final Map<Document, InputStream> documents = createDocumentMap();
        Message message = createMessage();
        final DigipostClientConfig config = DigipostClientConfig.newConfiguration()
                .failOnHtmlSanitationDiff()
                .build();

        DigipostClientException thrown = assertThrows(DigipostClientException.class,
                () -> preparer.prepare(documents, message, encrypter, () -> PdfValidationSettings.CHECK_ALL, config)
        );
        assertThat(thrown, where(Exception::getMessage, containsString("HTML_CONTENT_SANITIZED: Kjør DigipostValidatingHtmlSanitizer")));
    }

    @Test
    void sanitationDiffGeneratesWarning() throws IOException {
        final Map<Document, InputStream> documents = createDocumentMap();
        Message message = createMessage();
        final DigipostClientConfig config = DigipostClientConfig.newConfiguration()
                .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stdOut = System.err;
        try {
            System.setErr(new PrintStream(baos));
            preparer.prepare(documents, message, encrypter, () -> PdfValidationSettings.CHECK_ALL, config);
            System.err.flush();
        } finally {
            System.setErr(stdOut);
        }
        assertThat(baos.toString(),
                matchesPattern("(?s).* Din html vil forandre seg .*\\nKjør DigipostValidatingHtmlSanitizer.*"));
    }

    private Map<Document, InputStream> createDocumentMap() {
        final Document primary = new Document(UUID.randomUUID(), "primary", HTML);
        primary.encrypt();
        final Map<Document, InputStream> documents =
                Collections.singletonMap(primary, EksempelHtml.validNotSanitizedHtml());
        return documents;
    }

    private Message createMessage() {
        final MessageBuilder messageBuilder = Message.newMessage(UUID.randomUUID(), primaryDocument).recipient(new DigipostAddress("test.testson#1234"));
        Message message = messageBuilder.build();
        return message;
    }

    @Test
    public void failesDocumentWithValidation() throws IOException {
        final Document primary = new Document(UUID.randomUUID(), "primary", HTML);
        primary.encrypt();
        final Map<Document, InputStream> documents = new HashMap<Document, InputStream>() {{
            put(primary, EksempelHtml.illegalTags());
        }};
        final MessageBuilder messageBuilder = Message.newMessage(UUID.randomUUID(), primaryDocument).recipient(new DigipostAddress("test.testson#1234"));

        final DigipostClientConfig config = DigipostClientConfig.newConfiguration()
                .build();

        DigipostClientException thrown = assertThrows(DigipostClientException.class,
                () -> preparer.prepare(documents, messageBuilder.build(), encrypter, () -> PdfValidationSettings.CHECK_ALL, config));
        assertThat(thrown, where(Exception::getMessage, allOf(containsString("INVALID_HTML_CONTENT"), containsString("Tag name: script"))));
    }

    @Test
    public void doesNothingForNonPreEncryptedDocuments() throws IOException {
        Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT, () -> PdfValidationSettings.CHECK_ALL, DigipostClientConfig.newConfiguration().build());

        assertThat(documents.keySet(), contains(primaryDocument));
        assertThat(documents.get(primaryDocument), sameInstance(preparedDocuments.get(primaryDocument)));
    }

    @Test
    public void dontInsertDocumentsPreparerTestBlankPageAfterPrimaryDocumentForPreEncryptedDocuments() throws IOException {
        primaryDocument.encrypt();
        addAttachment("attachment", PDF, printablePdf2Pages()).encrypt();
        Message message = messageBuilder.build();
        Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter, () -> PdfValidationSettings.CHECK_ALL, DigipostClientConfig.newConfiguration().build());

        assertThat(preparedDocuments.size(), is(2));
    }

    private Document addAttachment(String subject, FileType fileType, InputStream content) {
        Document document = new Document(UUID.randomUUID(), subject, fileType);
        documents.put(document, content);
        messageBuilder.attachments(singletonList(document));
        return document;
    }

}
