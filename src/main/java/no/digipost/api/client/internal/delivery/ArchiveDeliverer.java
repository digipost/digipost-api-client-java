/*
 * Copyright (C) Posten Bring AS
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
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.archive.ArchiveApi;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.MediaTypes;
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static no.digipost.api.client.internal.http.response.HttpResponseUtils.checkResponse;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MULTI_MEDIA_SUB_TYPE_V8;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class ArchiveDeliverer {

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveDeliverer.class);

    private final ArchiveApi apiService;
    private final EventLogger eventLogger;

    public ArchiveDeliverer(DigipostClientConfig config, ArchiveApi apiService) {
        this.eventLogger = config.eventLogger.withDebugLogTo(LOG);
        this.apiService  = apiService;
    }

    public ArchiveApi.ArchivingDocuments createArchive(Archive archive) {
        return new PerformingArchivingDocuments(archive, this);
    }

    /**
     * Sender melding med alle dokumenter og innhold med én API-forespørsel (HTTP multipart request).
     */
    public Archive sendMultipartMessage(Archive archive, Map<UUID, DocumentContent> documentsAndContent) {

        final Map<ArchiveDocument, InputStream> documentInputStream = archive.getDocuments().stream()
                .collect(toMap(identity(), (doc) -> documentsAndContent.get(doc.getUuid()).getDigipostContent()));

        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            marshal(jaxbContext, archive, bao);
            ByteArrayBody attachment = new ByteArrayBody(bao.toByteArray(),
                    ContentType.create(MediaTypes.DIGIPOST_MEDIA_TYPE_V8, UTF_8), "archive");

            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .setMimeSubtype(DIGIPOST_MULTI_MEDIA_SUB_TYPE_V8)
                    .addPart(FormBodyPartBuilder.create("archive", attachment)
                            .addField("Content-Disposition", "attachment;" + " filename=\"archive\"")
                            .build());

            for (Entry<ArchiveDocument, InputStream> documentAndContent : documentInputStream.entrySet()) {
                ArchiveDocument document = documentAndContent.getKey();
                InputStream content = documentAndContent.getValue();


                byte[] bytes = IOUtils.toByteArray(content);
                multipartEntity = multipartEntity
                        .addPart(FormBodyPartBuilder
                                .create("application", new ByteArrayBody(bytes, ContentType.create(defaultIfBlank(document.getContentType(), "application/octet-stream")), document.uuid.toString()))
                                .addField("Content-Disposition", "attachment;" + " filename=\"" + document.uuid.toString() + "\"").build());
            }
            eventLogger.log("*** STARTER INTERAKSJON MED API: Arkiverer filer ***");
            try (CloseableHttpResponse response = apiService.sendMultipartArchive(multipartEntity.build())) {
                checkResponse(response, eventLogger);

                eventLogger.log("Arkivdokumentet ble sendt. Status: [" + response + "]");

                return unmarshal(jaxbContext, response.getEntity().getContent(), Archive.class);

            } catch (IOException e) {
                throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e.getMessage());
            }

        } catch (Exception e) {
            throw DigipostClientException.from(e);
        }
    }


}
