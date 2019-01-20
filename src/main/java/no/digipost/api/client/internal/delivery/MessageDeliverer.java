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
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.delivery.MessageDeliveryApi;
import no.digipost.api.client.delivery.OngoingDelivery;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.AddDataLink;
import no.digipost.api.client.representations.AdditionalData;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.EncryptionKey;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResultCode;
import no.digipost.api.client.representations.IdentificationResultWithEncryptionKey;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MediaTypes;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.security.DigipostPublicKey;
import no.digipost.api.client.security.Encrypter;
import no.digipost.print.validate.PdfValidator;
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
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ZERO;
import static java.time.Duration.between;
import static java.util.Optional.empty;
import static no.digipost.api.client.internal.ExceptionUtils.asUnchecked;
import static no.digipost.api.client.internal.http.response.HttpResponseUtils.checkResponse;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MULTI_MEDIA_SUB_TYPE_V7;
import static no.digipost.api.client.security.Encrypter.FAIL_IF_TRYING_TO_ENCRYPT;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class MessageDeliverer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDeliverer.class);

    private final Clock clock;
    private final DocumentsPreparer documentsPreparer;
    private final DigipostClientConfig config;
    private final MessageDeliveryApi apiService;
    private final EventLogger eventLogger;

    private Instant printKeyCachedTime = Instant.MIN;
    private DigipostPublicKey cachedPrintKey;


    public MessageDeliverer(DigipostClientConfig config, MessageDeliveryApi apiService) {
        this(config, apiService, new DocumentsPreparer(new PdfValidator()));
    }

    public MessageDeliverer(DigipostClientConfig config, MessageDeliveryApi apiService, DocumentsPreparer documentsPreparer) {
        this.eventLogger = config.eventLogger.withDebugLogTo(LOG);
        this.config = config;
        this.apiService = apiService;
        this.documentsPreparer = documentsPreparer;
        this.clock = config.clock;
    }

    public OngoingDelivery.WithPrintFallback createMessage(Message message) {
        return new WithPrintFallback(message, this);
    }

    public OngoingDelivery.ForPrintOnly createPrintOnlyMessage(Message printMessage) {
        return new PrintOnlyMessage(printMessage, this);
    }



    /**
     * Sender melding med alle dokumenter og innhold med én API-forespørsel (HTTP multipart request).
     * Dersom dokumentene skal direkte til print og skal prekrypteres før sending kan det gjøres en ekstra request for å hente
     * krypteringsnøkkel.
     */
    public MessageDelivery sendMultipartMessage(Message message, Map<String, DocumentContent> documentsAndContent) {
        EncryptionKeyAndDocsWithInputstream encryptionAndInputStream = fetchEncryptionKeyForRecipientIfNecessaryAndMapContentToInputstream(message, documentsAndContent);
        Encrypter encrypter = encryptionAndInputStream.digipostPublicKeys.map(Encrypter::using).orElse(FAIL_IF_TRYING_TO_ENCRYPT);
        Map<Document, InputStream> documentInputStream = encryptionAndInputStream.documentsAndInputstream;
        Message singleChannelMessage = encryptionAndInputStream.getSingleChannelMessage();

        try {
            Map<Document, InputStream> preparedDocuments = documentsPreparer.prepare(
                    documentInputStream, singleChannelMessage, encrypter, () -> apiService.getSenderInformation(message).getPdfValidationSettings());

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            marshal(jaxbContext, singleChannelMessage, bao);
            ByteArrayBody attachment = new ByteArrayBody(bao.toByteArray(),
                    ContentType.create(MediaTypes.DIGIPOST_MEDIA_TYPE_V7, UTF_8), "message");

            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .setMimeSubtype(DIGIPOST_MULTI_MEDIA_SUB_TYPE_V7)
                    .addPart(FormBodyPartBuilder.create("message", attachment)
                            .addField("Content-Disposition", "attachment;" + " filename=\"message\"")
                            .build());

            for (Entry<Document, InputStream> documentAndContent : preparedDocuments.entrySet()) {
                Document document = documentAndContent.getKey();
                InputStream content = documentAndContent.getValue();


                byte[] bytes = IOUtils.toByteArray(content);
                multipartEntity = multipartEntity
                        .addPart(FormBodyPartBuilder
                        .create("application", new ByteArrayBody(bytes, ContentType.create("application/" + defaultIfBlank(document.getDigipostFileType(), "octet-stream")), document.uuid.toString()))
                        .addField("Content-Disposition", "attachment;" + " filename=\"" + document.uuid.toString() + "\"").build());
            }
            eventLogger.log("*** STARTER INTERAKSJON MED API: SENDER MELDING MED ID " + singleChannelMessage.messageId + " ***");
            try (CloseableHttpResponse response = apiService.sendMultipartMessage(multipartEntity.build())) {
                checkResponse(response, eventLogger);

                eventLogger.log("Brevet ble sendt. Status: [" + response + "]");

                return unmarshal(jaxbContext, response.getEntity().getContent(), MessageDelivery.class);

            } catch (IOException e) {
                throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e.getMessage());
            }

        } catch (Exception e) {
            throw DigipostClientException.from(e);
        }
    }


    public void addData(AddDataLink addDataLink, AdditionalData data) {
        eventLogger.log("*** STARTER INTERAKSJON MED API: LEGGER TIL DATA PÅ DOKUMENT ***");
        try (CloseableHttpResponse response = apiService.addData(addDataLink, data)) {

            checkResponse(response, eventLogger);

            eventLogger.log("Data ble lagt til dokument. Status: [" + response.toString() + "]");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }



    /**
     * Henter brukers public nøkkel fra serveren og krypterer brevet som skal
     * sendes med denne.
     */
    public InputStream fetchKeyAndEncrypt(Document document, InputStream content) {
        checkThatMessageCanBePreEncrypted(document);

        try(CloseableHttpResponse encryptionKeyResponse = apiService.getEncryptionKey(document.getEncryptionKeyLink().getUri())){
            checkResponse(encryptionKeyResponse, eventLogger);

            EncryptionKey key = unmarshal(jaxbContext, encryptionKeyResponse.getEntity().getContent(), EncryptionKey.class);
            return Encrypter.using(new DigipostPublicKey(key)).encrypt(content);
        } catch (IOException e) {
            throw asUnchecked(e);
        }

    }

    public IdentificationResultWithEncryptionKey identifyAndGetEncryptionKey(Identification identification) {
        try(CloseableHttpResponse response = apiService.identifyAndGetEncryptionKey(identification)){
            checkResponse(response, eventLogger);
            IdentificationResultWithEncryptionKey result =
                    unmarshal(jaxbContext, response.getEntity().getContent(), IdentificationResultWithEncryptionKey.class);
            if (result.getResultCode() == IdentificationResultCode.DIGIPOST) {
                if (result.getEncryptionKey() == null) {
                    throw new DigipostClientException(ErrorCode.SERVER_ERROR, "Server identifisert mottaker som Digipost-bruker, men sendte ikke med krypteringsnøkkel. Indikerer en feil hos Digipost.");
                }
                eventLogger.log("Mottaker er Digipost-bruker. Hentet krypteringsnøkkel.");
            } else {
                eventLogger.log("Mottaker er ikke Digipost-bruker.");
            }
            return result;
        } catch (IOException e) {
            throw asUnchecked(e);
        }
    }

    public DigipostPublicKey getEncryptionKeyForPrint() {
        Instant now = clock.instant();

        if (ZERO.equals(config.printKeyCacheTimeToLive) || between(printKeyCachedTime, now).compareTo(config.printKeyCacheTimeToLive) > 0) {
            eventLogger.log("*** STARTER INTERAKSJON MED API: HENT KRYPTERINGSNØKKEL FOR PRINT ***");
            try(CloseableHttpResponse response = apiService.getEncryptionKeyForPrint()) {
                checkResponse(response, eventLogger);
                EncryptionKey encryptionKey = unmarshal(jaxbContext, response.getEntity().getContent(), EncryptionKey.class);
                cachedPrintKey = new DigipostPublicKey(encryptionKey);
                printKeyCachedTime = now;
                return cachedPrintKey;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            eventLogger.log("Bruker cachet krypteringsnøkkel for print");
            return cachedPrintKey;
        }
    }



    private void checkThatMessageCanBePreEncrypted(final Document document) {
        Link encryptionKeyLink = document.getEncryptionKeyLink();
        if (encryptionKeyLink == null) {
            String errorMessage = "Document med id [" + document.uuid + "] kan ikke prekrypteres.";
            eventLogger.log(errorMessage);
            throw new DigipostClientException(ErrorCode.CANNOT_PREENCRYPT, errorMessage);
        }
    }


    private EncryptionKeyAndDocsWithInputstream fetchEncryptionKeyForRecipientIfNecessaryAndMapContentToInputstream(Message message,
                                                                                        Map<String, DocumentContent> documentsAndContent) {
        final Map<Document, InputStream> documentsAndInputstream = new LinkedHashMap<>();
        Optional<DigipostPublicKey> publicKeys = empty();
        Message singleChannelMessage;

            if (message.isDirectPrint()) {
                singleChannelMessage = setMapAndMessageToPrint(message, documentsAndContent, documentsAndInputstream);

                if (singleChannelMessage.hasAnyDocumentRequiringEncryption()) {
                    eventLogger.log("Direkte print. Bruker krypteringsnøkkel for print.");
                    publicKeys = Optional.ofNullable(getEncryptionKeyForPrint());
                }

            } else if (!message.recipient.hasPrintDetails() && !message.hasAnyDocumentRequiringEncryption()) {
                singleChannelMessage = setMapAndMessageToDigipost(message, documentsAndContent, documentsAndInputstream);

            } else {
                IdentificationResultWithEncryptionKey result = identifyAndGetEncryptionKey(message.recipient.toIdentification());
                if (result.getResultCode() == IdentificationResultCode.DIGIPOST) {
                    singleChannelMessage = setMapAndMessageToDigipost(message, documentsAndContent, documentsAndInputstream);

                    if (singleChannelMessage.hasAnyDocumentRequiringEncryption()) {
                        eventLogger.log("Mottaker er Digipost-bruker. Bruker brukers krypteringsnøkkel.");
                        publicKeys = Optional.of(new DigipostPublicKey(result.getEncryptionKey()));
                    }
                } else if (message.recipient.hasPrintDetails()) {
                    singleChannelMessage = setMapAndMessageToPrint(message, documentsAndContent, documentsAndInputstream);

                    if (singleChannelMessage.hasAnyDocumentRequiringEncryption()) {
                        eventLogger.log("Mottaker er ikke Digipost-bruker. Bruker krypteringsnøkkel for print.");
                        publicKeys = Optional.of(getEncryptionKeyForPrint());
                    }
                } else {
                    throw new DigipostClientException(ErrorCode.UNKNOWN_RECIPIENT, "Mottaker er ikke Digipost-bruker og forsendelse mangler print-fallback.");
                }
            }
        return new EncryptionKeyAndDocsWithInputstream(publicKeys, documentsAndInputstream, singleChannelMessage);
    }

    static Message setMapAndMessageToDigipost(Message messageToCopy, Map<String, DocumentContent> documentsAndContent,
                                              Map<Document, InputStream> documentsAndInputStream){
        Message singleChannelMessage = Message.copyMessageWithOnlyDigipostDetails(messageToCopy);
        setDigipostContentToUUID(documentsAndContent, documentsAndInputStream, singleChannelMessage.getAllDocuments());

        return singleChannelMessage;
    }

    static Message setMapAndMessageToPrint(Message messageToCopy, Map<String, DocumentContent> documentsAndContent,
                                           Map<Document, InputStream> documentsAndInputStream) {
        Message singleChannelMessage = Message.copyMessageWithOnlyPrintDetails(messageToCopy);
        setPrintContentToUUID(documentsAndContent, documentsAndInputStream, singleChannelMessage.getAllDocuments());

        return singleChannelMessage;
    }

    static void setDigipostContentToUUID(Map<String, DocumentContent> documentsAndContent, Map<Document, InputStream> documentsAndInputstream, Stream<Document> allDocuments) {
        allDocuments.forEach(doc -> documentsAndInputstream.put(doc, documentsAndContent.get(doc.uuid).getDigipostContent()));
    }

    static void setPrintContentToUUID(Map<String, DocumentContent> documentsAndContent, Map<Document, InputStream> documentsAndInputstream, Stream<Document> allDocuments) {
        allDocuments.forEach(doc -> documentsAndInputstream.put(doc, documentsAndContent.get(doc.uuid).getPrintContent()));
    }

    private static class EncryptionKeyAndDocsWithInputstream {
        public final Optional<DigipostPublicKey> digipostPublicKeys;
        public final Map<Document, InputStream> documentsAndInputstream;
        private final Message singleChannelMessage;

        public EncryptionKeyAndDocsWithInputstream(Optional<DigipostPublicKey> digipostPublicKeys,
                               Map<Document, InputStream> documentsAndInputstream, Message singleChannelMessage) {
            this.digipostPublicKeys = digipostPublicKeys;
            this.documentsAndInputstream = documentsAndInputstream;
            this.singleChannelMessage = singleChannelMessage;
        }

        public Message getSingleChannelMessage() {
            return singleChannelMessage;
        }
    }

}
