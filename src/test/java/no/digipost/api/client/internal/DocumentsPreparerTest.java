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
package no.digipost.api.client.internal;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.internal.DocumentsPreparer;
import no.digipost.api.client.representations.Channel;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.FileType;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.Message.MessageBuilder;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PrintDetails;
import no.digipost.api.client.representations.PrintRecipient;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.security.Encrypter;
import no.digipost.api.client.security.FakeEncryptionKey;
import no.digipost.api.client.util.DigipostPublicKey;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static no.digipost.api.client.pdf.EksempelPdf.pdf20Pages;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf1Page;
import static no.digipost.api.client.pdf.EksempelPdf.printablePdf2Pages;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.FileType.GIF;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.security.Encrypter.FAIL_IF_TRYING_TO_ENCRYPT;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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


    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final DocumentsPreparer preparer = new DocumentsPreparer(new PdfValidator());

    private final Encrypter encrypter = Encrypter.using(new DigipostPublicKey(FakeEncryptionKey.createFakeEncryptionKey()));

    private final Document primaryDocument = new Document(UUID.randomUUID().toString(), "primary", PDF);
    private final Map<Document, InputStream> documents = new HashMap<Document, InputStream>() {{ put(primaryDocument, printablePdf1Page()); }};
    private final MessageBuilder messageBuilder = MessageBuilder.newMessage("m_id", primaryDocument).printDetails(
            new PrintDetails(new PrintRecipient("Joe Schmoe", new NorwegianAddress("7845", "Far away")), new PrintRecipient("Dolly Parton", new NorwegianAddress("8942", "Farther away"))));

    @Test
    public void failsIfMessageHasAnyDocumentsRequiringPreEncryptionAndNoEncryptionKeyIsSupplied() throws IOException {
        primaryDocument.encrypt();

        expectedException.expect(DigipostClientException.class);
        expectedException.expectMessage("no encryption key");
        preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT, () -> PdfValidationSettings.CHECK_ALL);
    }

    @Test
    public void cannotSendNonPdfFilesToPrint() throws IOException {
        addAttachment("funny animated gif", GIF, toInputStream("content doesn't matter", UTF_8)).encrypt();

        expectedException.expect(DigipostClientException.class);
        expectedException.expectMessage("filetype gif");
        preparer.prepare(documents, messageBuilder.build(), encrypter, () -> mock(PdfValidationSettings.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS)));
    }

    @Test
    public void deniesNonValidatingPdfForBothPrintAndWeb() {
        for (Channel deliveryMethod : Channel.values()) {
            try {
                preparer.validateAndSetNrOfPages(deliveryMethod, new Document(UUID.randomUUID().toString(), null, PDF), new byte[]{65, 65, 65, 65}, () -> mock(PdfValidationSettings.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS)));
            } catch (DigipostClientException e) {
                assertThat(e.getMessage(), containsString("Could not parse"));
                continue;
            }
            fail("Should fail validation for bogus PDF using " + Channel.class.getSimpleName() + "." + deliveryMethod);
        }
    }

    @Test
    public void passesDocumentForWebWhichWouldNotBeOkForPrint() throws IOException {
        preparer.validateAndSetNrOfPages(DIGIPOST, new Document(UUID.randomUUID().toString(), null, PDF), pdf20Pages, () -> PdfValidationSettings.CHECK_ALL);

        expectedException.expect(DigipostClientException.class);
        expectedException.expectMessage("too many pages");
        preparer.validateAndSetNrOfPages(PRINT, new Document(UUID.randomUUID().toString(), null, PDF), pdf20Pages, () -> PdfValidationSettings.CHECK_ALL);
    }


    @Test
    public void doesNothingForNonPreEncryptedDocuments() throws IOException {
        Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT, () -> PdfValidationSettings.CHECK_ALL);

        assertThat(documents.keySet(), contains(primaryDocument));
        assertThat(documents.get(primaryDocument), sameInstance(preparedDocuments.get(primaryDocument)));
    }

    @Test
    public void dontInsertDocumentsPreparerTestBlankPageAfterPrimaryDocumentForPreEncryptedDocuments() throws IOException {
        primaryDocument.encrypt();
        addAttachment("attachment", PDF, printablePdf2Pages()).encrypt();
        Message message = messageBuilder.build();
        Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter, () -> PdfValidationSettings.CHECK_ALL);

        assertThat(preparedDocuments.size(), is(2));
    }

    private Document addAttachment(String subject, FileType fileType, InputStream content) {
        Document document = new Document(UUID.randomUUID().toString(), subject, fileType);
        documents.put(document, content);
        messageBuilder.attachments(asList(document));
        return document;
    }

}
