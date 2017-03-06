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
import no.digipost.api.client.representations.Channel;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.util.Encrypter;
import no.digipost.print.validate.PdfValidationResult;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.print.validate.PdfValidationResult.EVERYTHING_OK;
import static org.apache.commons.io.IOUtils.toByteArray;

class DocumentsPreparer {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsPreparer.class);

    private final PdfValidator pdfValidator;

    DocumentsPreparer(PdfValidator pdfValidator) {
        this.pdfValidator = pdfValidator;
    }



    Map<Document, InputStream> prepare(
            Map<Document, InputStream> documentsAndContent, Message message,
            Encrypter encrypter, Supplier<PdfValidationSettings> pdfValidationSettings) throws IOException {

        final Map<Document, InputStream> prepared = new LinkedHashMap<>();

        if(message.recipient.hasPrintDetails() && message.recipient.hasDigipostIdentification()){
            throw new IllegalStateException("Forventet message med enkelt kanal");
        }

        for (Document document : (Iterable<Document>) documentsAndContent.keySet().stream().sorted(message.documentOrder())::iterator) {
            if (document.willBeEncrypted()) {
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



    Optional<PdfInfo> validateAndSetNrOfPages(Channel channel, Document document, byte[] content, Supplier<PdfValidationSettings> pdfValidationSettings) {
        if (channel == PRINT && !document.is(PDF)) {
            throw new DigipostClientException(ErrorCode.INVALID_PDF_CONTENT,
                    "PDF is required for direct-to-print messages. Document with uuid " + document.uuid + " had filetype " + document.getDigipostFileType());
        }

        PdfValidationResult pdfValidation;
        Optional<PdfInfo> pdfInfo;
        if (document.is(PDF)) {
            LOG.debug("Validerer PDF-dokument med uuid '{}'", document.uuid);
            pdfValidation = pdfValidator.validate(content, pdfValidationSettings.get());
            if (document.willBeEncrypted()) {
                document.setNumberOfEncryptedPages(pdfValidation.pages);
            }
            pdfInfo = Optional.of(new PdfInfo(pdfValidation.pages));
        } else {
            pdfValidation = EVERYTHING_OK;
            pdfInfo = empty();
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
