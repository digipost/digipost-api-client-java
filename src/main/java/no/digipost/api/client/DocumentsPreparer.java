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

import no.digipost.api.client.delivery.DocumentContent;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.pdf.BlankPdf;
import no.digipost.api.client.representations.Channel;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.util.Encrypter;
import no.digipost.print.validate.PdfValidationResult;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import no.motif.f.Fn0;
import no.motif.single.Elem;
import no.motif.single.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.print.validate.PdfValidationResult.EVERYTHING_OK;
import static no.motif.Iterate.on;
import static no.motif.Singular.none;
import static no.motif.Singular.optional;
import static org.apache.commons.io.IOUtils.toByteArray;

class DocumentsPreparer {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentsPreparer.class);

	private final PdfValidator pdfValidator;

	DocumentsPreparer(PdfValidator pdfValidator) {
	    this.pdfValidator = pdfValidator;
    }



	Map<Document, InputStream> prepare(
			Map<Document, InputStream> documentsAndContent, Message message,
			Encrypter encrypter, Fn0<PdfValidationSettings> pdfValidationSettings) throws IOException {

		final Map<Document, InputStream> prepared = new LinkedHashMap<>();

		if(message.recipient.hasPrintDetails() && message.recipient.hasDigipostIdentification()){
			throw new IllegalStateException("Forventet message med enkelt kanal");
		}

		for (Elem<Document> i : on(on(documentsAndContent.keySet()).sorted(message.documentOrder())).indexed()) {
			Document document = i.value;
			if (document.isPreEncrypt()) {
				byte[] byteContent = toByteArray(documentsAndContent.get(document));
				LOG.debug("Validerer dokument med uuid '{}' før kryptering", document.uuid);
				validateAndSetNrOfPages(message.getChannel(), document, byteContent, pdfValidationSettings);
				LOG.debug("Krypterer innhold for dokument med uuid '{}'", document.uuid);
				prepared.put(document, encrypter.encrypt(byteContent));

			} else {
				prepared.put(document, documentsAndContent.get(document));
			}
		}
		return prepared;
	}



	Optional<PdfInfo> validateAndSetNrOfPages(Channel channel, Document document, byte[] content, Fn0<PdfValidationSettings> pdfValidationSettings) {
		if (channel == PRINT && !document.is(PDF)) {
	    	throw new DigipostClientException(ErrorCode.INVALID_PDF_CONTENT,
	    			"PDF is required for direct-to-print messages. Document with uuid " + document.uuid + " had filetype " + document.getDigipostFileType());
	    }

		PdfValidationResult pdfValidation;
		Optional<PdfInfo> pdfInfo;
		if (document.is(PDF)) {
			LOG.debug("Validerer PDF-dokument med uuid '{}'", document.uuid);
			pdfValidation = pdfValidator.validate(content, pdfValidationSettings.$());
			document.setNoEncryptedPages(pdfValidation.pages);
			pdfInfo = optional(new PdfInfo(pdfValidation.pages));
		} else {
			pdfValidation = EVERYTHING_OK;
			pdfInfo = none();
		}

	    if ((channel == PRINT && !pdfValidation.okForPrint) || !pdfValidation.okForWeb) {
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
