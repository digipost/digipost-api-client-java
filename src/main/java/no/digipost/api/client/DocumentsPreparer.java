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
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.pdf.BlankPdf;
import no.digipost.api.client.representations.DeliveryMethod;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.util.Encrypter;
import no.digipost.print.validate.PdfValidationResult;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import no.motif.single.Elem;
import no.motif.single.Optional;
import no.motif.types.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static no.digipost.api.client.representations.DeliveryMethod.PRINT;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.print.validate.PdfValidationResult.EVERYTHING_OK;
import static no.digipost.print.validate.PdfValidationSettings.SJEKK_ALLE;
import static no.motif.Base.not;
import static no.motif.Base.where;
import static no.motif.Iterate.on;
import static no.motif.Singular.none;
import static no.motif.Singular.optional;
import static org.apache.commons.io.IOUtils.toByteArray;

class DocumentsPreparer {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentsPreparer.class);

	private final PdfValidator pdfValidator;

	private PdfValidationSettings pdfValidationSettings;

	DocumentsPreparer(PdfValidator pdfValidator) {
	    this.pdfValidator = pdfValidator;
	    this.pdfValidationSettings = SJEKK_ALLE;
    }

	void setPdfValidationSettings(PdfValidationSettings settings) {
		this.pdfValidationSettings = settings;
	}



	Map<Document, InputStream> prepare(Map<Document, InputStream> documentsAndContent, Message message, Encrypter encrypter) throws IOException {

		final int documentAmount = documentsAndContent.size();
		final Elements<Document> allDocuments = on(documentsAndContent.keySet());
		final boolean multipleDocumentsOnlyPdf = documentAmount > 1 && !allDocuments.exists(where(Document.getFileType, not(PDF)));

		final Map<Document, InputStream> prepared = new LinkedHashMap<>();

		for (Elem<Document> i : on(allDocuments.sorted(message.documentOrder())).indexed()) {
			Document document = i.value;
			if (document.isPreEncrypt()) {
				byte[] byteContent = toByteArray(documentsAndContent.get(document));
				Optional<PdfInfo> pdfInfo = validate(message.getDeliveryMethod(), document, byteContent);
				LOG.debug("Krypterer innhold for dokument med uuid '{}'", document.uuid);
				prepared.put(document, encrypter.encrypt(byteContent));

				if (multipleDocumentsOnlyPdf && i.index < documentAmount - 1 && pdfInfo.get().hasOddNumberOfPages) {
					Document blankPageDocument = Document.technicalAttachment(BlankPdf.TECHNICAL_TYPE, PDF);
					LOG.debug(
							"Dokument med uuid '{}' har {} sider. Legger til ekstra blank side " +
							"(uuid '{}') for å sikre at alle dokumenter begynner på nytt ark.",
							document.uuid, pdfInfo.get().pages, blankPageDocument.uuid);
					message.attachments.add(i.index, blankPageDocument);
					prepared.put(blankPageDocument, encrypter.encrypt(BlankPdf.onePage()));
				}
			} else {
				prepared.put(document, documentsAndContent.get(document));
			}
		}
		return prepared;
	}



	Optional<PdfInfo> validate(DeliveryMethod deliveryMethod, Document document, byte[] content) {
		if (deliveryMethod == PRINT && !document.is(PDF)) {
	    	throw new DigipostClientException(ErrorCode.INVALID_PDF_CONTENT,
	    			"PDF is required for direct-to-print messages. Document with uuid " + document.uuid + " had filetype " + document.getDigipostFileType());
	    }

		PdfValidationResult pdfValidation;
		Optional<PdfInfo> pdfInfo;
		if (document.is(PDF)) {
			LOG.debug("Validerer PDF-dokument med uuid '{}'", document.uuid);
			pdfValidation = pdfValidator.validate(content, pdfValidationSettings);
			pdfInfo = optional(new PdfInfo(pdfValidation.pages));
		} else {
			pdfValidation = EVERYTHING_OK;
			pdfInfo = none();
		}

	    if ((deliveryMethod == PRINT && !pdfValidation.okForPrint) || !pdfValidation.okForWeb) {
	    	throw new DigipostClientException(ErrorCode.INVALID_PDF_CONTENT, pdfValidation.toString());
	    }
	    return pdfInfo;
    }

	static class PdfInfo {
		final int pages;
		final boolean hasOddNumberOfPages;

		public PdfInfo(int numberOfPages) {
			this.pages = numberOfPages;
			this.hasOddNumberOfPages = pages % 2 == 1;
		}
	}

}
