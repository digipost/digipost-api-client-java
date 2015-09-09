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

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.Message.MessageBuilder;
import no.digipost.api.client.representations.PrintDetails.PostType;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.util.DigipostPublicKey;
import no.digipost.api.client.util.Encrypter;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Arrays.asList;
import static no.digipost.api.client.pdf.EksempelPdf.*;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.FileType.*;
import static no.digipost.api.client.util.Encrypter.FAIL_IF_TRYING_TO_ENCRYPT;
import static no.motif.Base.always;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public class DocumentsPreparerTest {

	private static final byte[] pdf20Pages;
	static {
		CryptoUtil.verifyJCE();
		try {
	        pdf20Pages = toByteArray(pdf20Pages());
        } catch (IOException e) {
	        throw new RuntimeException(e.getMessage(), e);
        }
	}


	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	private final DocumentsPreparer preparer = new DocumentsPreparer(new PdfValidator());

	private final Encrypter encrypter = Encrypter.using(new DigipostPublicKey(ApiServiceMock.createFakeEncryptionKey()));

	private final Document primaryDocument = new Document(UUID.randomUUID().toString(), "primary", PDF);
	private final Map<Document, InputStream> documents = new HashMap<Document, InputStream>() {{ put(primaryDocument, printablePdf1Page()); }};
	private final MessageBuilder messageBuilder = MessageBuilder.newMessage("m_id", primaryDocument).printDetails(
			new PrintDetails(new PrintRecipient("Joe Schmoe", new NorwegianAddress("7845", "Far away")), new PrintRecipient("Dolly Parton", new NorwegianAddress("8942", "Farther away")), PostType.A));

	@Test
	public void failsIfMessageHasAnyDocumentsRequiringPreEncryptionAndNoEncryptionKeyIsSupplied() throws IOException {
		primaryDocument.setPreEncrypt();

		expectedException.expect(DigipostClientException.class);
		expectedException.expectMessage("no encryption key");
		preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT, always(PdfValidationSettings.SJEKK_ALLE));
	}

	@Test
	public void cannotSendNonPdfFilesToPrint() throws IOException {
		addAttachment("funny animated gif", GIF, toInputStream("content doesn't matter")).setPreEncrypt();

		expectedException.expect(DigipostClientException.class);
		expectedException.expectMessage("filetype gif");
		preparer.prepare(documents, messageBuilder.build(), encrypter, always(mock(PdfValidationSettings.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS))));
	}

	@Test
	public void deniesNonValidatingPdfForBothPrintAndWeb() {
		for (Channel deliveryMethod : Channel.values()) {
			try {
				preparer.validateAndSetNrOfPages(deliveryMethod, new Document(UUID.randomUUID().toString(), null, PDF), new byte[]{65, 65, 65, 65}, always(mock(PdfValidationSettings.class, withSettings().defaultAnswer(RETURNS_SMART_NULLS))));
			} catch (DigipostClientException e) {
				assertThat(e.getMessage(), containsString("Kunne ikke parse"));
				continue;
			}
			fail("Should fail validation for bogus PDF using " + Channel.class.getSimpleName() + "." + deliveryMethod);
		}
	}

	@Test
	public void passesDocumentForWebWhichWouldNotBeOkForPrint() throws IOException {
		preparer.validateAndSetNrOfPages(DIGIPOST, new Document(UUID.randomUUID().toString(), null, PDF), pdf20Pages, always(PdfValidationSettings.SJEKK_ALLE));

		expectedException.expect(DigipostClientException.class);
		expectedException.expectMessage("for mange sider");
		preparer.validateAndSetNrOfPages(PRINT, new Document(UUID.randomUUID().toString(), null, PDF), pdf20Pages, always(PdfValidationSettings.SJEKK_ALLE));
	}


	@Test
	public void doesNothingForNonPreEncryptedDocuments() throws IOException {
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT, always(PdfValidationSettings.SJEKK_ALLE));

		assertThat(documents.keySet(), contains(primaryDocument));
		assertThat(documents.get(primaryDocument), sameInstance(preparedDocuments.get(primaryDocument)));
	}

	@Test
	public void validatesAndEncryptsWithNoBlankPageInsertedAfterOddNumberOfPagesPrimaryDocument() throws IOException {
		primaryDocument.setPreEncrypt();
		Message message = messageBuilder.build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter, always(PdfValidationSettings.SJEKK_ALLE));

		assertThat(message.getAllDocuments(), hasSize(1));
		assertThat(message.getAllDocuments(), contains(primaryDocument));
		assertThat(preparedDocuments.keySet(), contains(primaryDocument));
		assertThat(toByteArray(preparedDocuments.get(primaryDocument)), not(toByteArray(printablePdf1Page())));
	}

	@Test
	public void insertsBlankPageAfterPrimaryDocumentForPreEncryptedDocuments() throws IOException {
		primaryDocument.setPreEncrypt();
		Document attachment = addAttachment("attachment", PDF, printablePdf2Pages()).setPreEncrypt();
		Message message = messageBuilder.build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter, always(PdfValidationSettings.SJEKK_ALLE));

		@SuppressWarnings("unchecked")
        Matcher<Iterable<? extends Document>> primaryDocThenBlankPageThenAttachment = contains(is(primaryDocument), blankPdf, is(attachment));
		assertThat(preparedDocuments.keySet(), primaryDocThenBlankPageThenAttachment);
		assertThat(message.getAllDocuments(), primaryDocThenBlankPageThenAttachment);
	}

	@Test
	public void neverInsertsBlankPageAfterLastAttachment() throws IOException {
		primaryDocument.setPreEncrypt();
		documents.put(primaryDocument, printablePdf2Pages());
		Document a1 = addAttachment("attachment", PDF, printablePdf1Page()).setPreEncrypt();
		Document a2 = addAttachment("attachment", PDF, printablePdf1Page()).setPreEncrypt();
		Message message = messageBuilder.build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter, always(PdfValidationSettings.SJEKK_ALLE));

		@SuppressWarnings("unchecked")
        Matcher<Iterable<? extends Document>> blankPageOnlyAfterFirstAttachment = contains(is(primaryDocument), is(a1), blankPdf, is(a2));
		assertThat(preparedDocuments.keySet(), blankPageOnlyAfterFirstAttachment);
		assertThat(message.getAllDocuments(), blankPageOnlyAfterFirstAttachment);
	}

	@Test
	public void nonPdfContentNeverInsertsBlankPage() throws IOException {
		primaryDocument.setPreEncrypt();
		Document a1 = addAttachment("attachment 1", HTML, toInputStream("content doesn't matter")).setPreEncrypt();
		Document a2 = addAttachment("attachment 2", JPG, toInputStream("content doesn't matter")).setPreEncrypt();
		Message message = messageBuilder.digipostAddress(new DigipostAddress("test#ABCD")).build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter, always(PdfValidationSettings.SJEKK_ALLE));

		assertThat(preparedDocuments.keySet(), contains(primaryDocument, a1, a2));
		assertThat(message.getAllDocuments(), contains(primaryDocument, a1, a2));
	}


	private Document addAttachment(String subject, FileType fileType, InputStream content) {
		Document document = new Document(UUID.randomUUID().toString(), subject, fileType);
		documents.put(document, content);
		messageBuilder.attachments(asList(document));
		return document;
	}


	private static final Matcher<Document> blankPdf = new CustomTypeSafeMatcher<Document>(Document.class.getSimpleName() + " for padding with a blank page") {
		@Override
        protected boolean matchesSafely(Document doc) {
			return doc.subject == null && doc.isPreEncrypt();
        }

		@Override
        protected void describeMismatchSafely(Document doc, Description mismatchDescription) {
			if (doc.subject != null) {
				mismatchDescription.appendText("has subject '").appendText(doc.subject).appendText("' (should be null) ");
			}
			if (!doc.isPreEncrypt()) {
				mismatchDescription.appendText("not marked as preencrypted");
			}
		};
	};


}
