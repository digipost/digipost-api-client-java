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
import no.digipost.api.client.pdf.BlankPdf;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.Message.MessageBuilder;
import no.digipost.api.client.representations.PrintDetails.PostType;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.util.DigipostPublicKey;
import no.digipost.api.client.util.Encrypter;
import no.digipost.print.validate.PdfValidator;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CustomTypeSafeMatcher;
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
import static no.digipost.api.client.representations.DeliveryMethod.DIGIPOST;
import static no.digipost.api.client.representations.DeliveryMethod.PRINT;
import static no.digipost.api.client.representations.FileType.*;
import static no.digipost.api.client.util.Encrypter.FAIL_IF_TRYING_TO_ENCRYPT;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
		preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT);
	}

	@Test
	public void cannotSendNonPdfFilesToPrint() throws IOException {
		Document gif = new Document(UUID.randomUUID().toString(), "funny animated gif", GIF).setPreEncrypt();
		documents.put(gif, toInputStream("content doesn't matter"));
		Message message = messageBuilder.attachments(asList(gif)).build();

		expectedException.expect(DigipostClientException.class);
		expectedException.expectMessage("filetype gif");
		preparer.prepare(documents, message, encrypter);
	}

	@Test
	public void deniesNonValidatingPdfForBothPrintAndWeb() {
		for (DeliveryMethod deliveryMethod : DeliveryMethod.values()) {
			try {
				preparer.validate(deliveryMethod, new Document(UUID.randomUUID().toString(), null, PDF), new byte[] {65, 65, 65, 65});
			} catch (DigipostClientException e) {
				assertThat(e.getMessage(), containsString("Kunne ikke parse"));
				continue;
			}
			fail("Should fail validation for bogus PDF using " + DeliveryMethod.class.getSimpleName() + "." + deliveryMethod);
		}
	}

	@Test
	public void passesDocumentForWebWhichWouldNotBeOkForPrint() throws IOException {
		preparer.validate(DIGIPOST, new Document(UUID.randomUUID().toString(), null, PDF), pdf20Pages);

		expectedException.expect(DigipostClientException.class);
		expectedException.expectMessage("for mange sider");
		preparer.validate(PRINT, new Document(UUID.randomUUID().toString(), null, PDF), pdf20Pages);
	}


	@Test
	public void doesNothingForNonPreEncryptedDocuments() throws IOException {
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, messageBuilder.build(), FAIL_IF_TRYING_TO_ENCRYPT);

		assertThat(documents.keySet(), contains(primaryDocument));
		assertThat(documents.get(primaryDocument), sameInstance(preparedDocuments.get(primaryDocument)));
	}

	@Test
	public void validatesAndEncryptsWithNoChanges() throws IOException {
		primaryDocument.setPreEncrypt();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, messageBuilder.build(), encrypter);

		assertThat(documents.keySet(), is(preparedDocuments.keySet()));
		assertThat(toByteArray(preparedDocuments.get(primaryDocument)), not(toByteArray(printablePdf1Page())));
	}

	@Test
	public void insertsBlankPageAfterPrimaryDocumentForPreEncryptedDocuments() throws IOException {
		primaryDocument.setPreEncrypt();
		Document attachment = new Document(UUID.randomUUID().toString(), "attachment", PDF).setPreEncrypt();
		documents.put(attachment, printablePdf2Pages());
		Message message = messageBuilder.attachments(asList(attachment)).build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter);

		@SuppressWarnings("unchecked")
        Matcher<Iterable<? extends Document>> primaryDocThenBlankPageThenAttachment = contains(is(primaryDocument), blankPage, is(attachment));
		assertThat(preparedDocuments.keySet(), primaryDocThenBlankPageThenAttachment);
		assertThat(message.getAllDocuments(), primaryDocThenBlankPageThenAttachment);
	}

	@Test
	public void neverInsertsBlankPageAfterLastAttachment() throws IOException {
		primaryDocument.setPreEncrypt();
		Document a1 = new Document(UUID.randomUUID().toString(), "attachment", PDF).setPreEncrypt();
		Document a2 = new Document(UUID.randomUUID().toString(), "attachment", PDF).setPreEncrypt();
		documents.put(primaryDocument, printablePdf2Pages());
		documents.put(a1, printablePdf1Page());
		documents.put(a2, printablePdf1Page());
		Message message = messageBuilder.attachments(asList(a1, a2)).build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter);

		@SuppressWarnings("unchecked")
        Matcher<Iterable<? extends Document>> blankPageOnlyAfterFirstAttachment = contains(is(primaryDocument), is(a1), blankPage, is(a2));
		assertThat(preparedDocuments.keySet(), blankPageOnlyAfterFirstAttachment);
		assertThat(message.getAllDocuments(), blankPageOnlyAfterFirstAttachment);
	}

	@Test
	public void nonPdfContentNeverInsertsBlankPage() throws IOException {
		primaryDocument.setPreEncrypt();
		Document a1 = new Document(UUID.randomUUID().toString(), "attachment", HTML).setPreEncrypt();
		Document a2 = new Document(UUID.randomUUID().toString(), "attachment", JPG).setPreEncrypt();
		documents.put(primaryDocument, printablePdf1Page());
		documents.put(a1, IOUtils.toInputStream("content doesn't matter"));
		documents.put(a2, IOUtils.toInputStream("content doesn't matter"));
		Message message = messageBuilder.digipostAddress(new DigipostAddress("test#ABCD")).attachments(asList(a1, a2)).build();
		Map<Document, InputStream> preparedDocuments = preparer.prepare(documents, message, encrypter);

		@SuppressWarnings("unchecked")
        Matcher<Iterable<? extends Document>> blankPageAfterPrimaryDocument = contains(is(primaryDocument), is(a1), is(a2));
		assertThat(preparedDocuments.keySet(), blankPageAfterPrimaryDocument);
		assertThat(message.getAllDocuments(), blankPageAfterPrimaryDocument);
	}


	private static final Matcher<Document> blankPage = new CustomTypeSafeMatcher<Document>(Document.class.getSimpleName() + " for padding with a blank page") {
		@Override
        protected boolean matchesSafely(Document doc) {
			return BlankPdf.TECHNICAL_TYPE.equals(doc.getTechnicalType());
        }
	};


}
