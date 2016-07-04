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
import no.digipost.api.client.representations.*;
import no.digipost.api.client.util.DigipostPublicKey;
import no.digipost.api.client.util.Encrypter;
import no.digipost.api.client.util.JAXBContextUtils;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import no.motif.f.Apply;
import no.motif.f.Fn;
import no.motif.single.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static no.digipost.api.client.errorhandling.ErrorCode.GENERAL_ERROR;
import static no.digipost.api.client.util.Encrypter.FAIL_IF_TRYING_TO_ENCRYPT;
import static no.digipost.api.client.util.Encrypter.keyToEncrypter;
import static no.digipost.api.client.util.JAXBContextUtils.*;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;
import static no.motif.Singular.*;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class MessageSender extends Communicator {

	private final Fn<MayHaveSender, PdfValidationSettings> resolvePdfValidationSettings = new Fn<MayHaveSender, PdfValidationSettings>() {
		@Override public PdfValidationSettings $(MayHaveSender message) {
			return apiService.getSenderInformation(message).getPdfValidationSettings();
		}};

	private final DocumentsPreparer documentsPreparer;
	private final DigipostClientConfig digipostClientConfig;

	private DateTime printKeyCachedTime = null;
	private DigipostPublicKey cachedPrintKey;


	public MessageSender(DigipostClientConfig digipostClientConfig, ApiService apiService, EventLogger eventLogger, PdfValidator pdfValidator) {
		super(apiService, eventLogger);
		this.documentsPreparer = new DocumentsPreparer(pdfValidator);
		this.digipostClientConfig = digipostClientConfig;
	}



	/**
	 * Sender melding med alle dokumenter og innhold med én API-forespørsel (HTTP multipart request).
	 * Dersom dokumentene skal direkte til print og skal prekrypteres før sending kan det gjøres en ekstra request for å hente
	 * krypteringsnøkkel.
	 */
	public MessageDelivery sendMultipartMessage(Message message, Map<String, DocumentContent> documentsAndContent) {
		EncryptionKeyAndDocsWithInputstream encryptionAndInputStream = fetchEncryptionKeyForRecipientIfNecessaryAndMapContentToInputstream(message, documentsAndContent);
		Encrypter encrypter = encryptionAndInputStream.digipostPublicKeys.map(keyToEncrypter).orElse(FAIL_IF_TRYING_TO_ENCRYPT);
		Map<Document, InputStream> documentInputStream = encryptionAndInputStream.documentsAndInputstream;
		Message singleChannelMessage = encryptionAndInputStream.getSingleChannelMessage();

		try {
			Map<Document, InputStream> preparedDocuments = documentsPreparer.prepare(
					documentInputStream, singleChannelMessage, encrypter, Apply.partially(resolvePdfValidationSettings).of(singleChannelMessage));

			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			marshal(messageContext, singleChannelMessage, bao);
			ByteArrayBody attachment = new ByteArrayBody(bao.toByteArray(),
					ContentType.create(MediaTypes.DIGIPOST_MEDIA_TYPE_V6), "message");

			MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.STRICT)
					.setMimeSubtype("mixed")
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
			log("*** STARTER INTERAKSJON MED API: SENDER MELDING MED ID " + singleChannelMessage.messageId + " ***");
			try(CloseableHttpResponse response = apiService.multipartMessage(multipartEntity.build())) {
				checkResponse(response);

				log("Brevet ble sendt. Status: [" + response + "]");

				return unmarshal(messageDeliveryContext, response.getEntity().getContent(), MessageDelivery.class);

			} catch (IOException e) {
				throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e.getMessage());
			}

		} catch (Exception e) {
			throw DigipostClientException.from(e);
		}
	}


	/**
	 * Oppretter en forsendelsesressurs på serveren eller henter en allerede
	 * opprettet forsendelsesressurs.
	 *
	 * Dersom forsendelsen allerede er opprettet, vil denne metoden gjøre en
	 * GET-forespørsel mot serveren for å hente en representasjon av
	 * forsendelsesressursen slik den er på serveren. Dette vil ikke føre til
	 * noen endringer av ressursen.
	 *
	 * Dersom forsendelsen ikke eksisterer fra før, vil denne metoden opprette
	 * en ny forsendelsesressurs på serveren og returnere en representasjon av
	 * ressursen.
	 *
	 */
	public MessageDelivery createOrFetchMessage(final Message message) {
		try(CloseableHttpResponse response = apiService.createMessage(message)){
			if (resourceAlreadyExists(response)) {
				try(CloseableHttpResponse existingMessageResponse = apiService.fetchExistingMessage(responseToURI(response))) {
					checkResponse(existingMessageResponse);
					try {
						MessageDelivery delivery = unmarshal(messageDeliveryContext, existingMessageResponse.getEntity().getContent(), MessageDelivery.class);
						checkThatExistingMessageIsIdenticalToNewMessage(delivery, message);
						checkThatMessageHasNotAlreadyBeenDelivered(delivery);
						log("Identisk forsendelse fantes fra før. Bruker denne istedenfor å opprette ny. Status: [" + response.toString() + "]");
						return delivery;
					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				}
			} else {
				try {
					checkResponse(response);
					log("Forsendelse opprettet. Status: [" + response.getStatusLine().getStatusCode() + "]");
					return unmarshal(messageDeliveryContext, response.getEntity().getContent(), MessageDelivery.class);

				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Legger til innhold til et dokument. For at denne metoden skal
	 * kunne kalles, må man først ha opprettet forsendelsesressursen på serveren
	 * ved metoden {@code createOrFetchMesssage}.
	 */
	public MessageDelivery addContent(final MessageDelivery message, final Document document, final InputStream documentContent, final InputStream printDocumentContent) {
		verifyCorrectStatus(message, MessageStatus.NOT_COMPLETE);
		final InputStream unencryptetContent;
		if (message.willBeDeliveredInDigipost()) {
			unencryptetContent = documentContent;
		} else {
			unencryptetContent = printDocumentContent;
			document.setDigipostFileType(FileType.PDF);
		}

		MessageDelivery delivery;
		if (document.isPreEncrypt()) {
			log("*** DOKUMENTET SKAL PREKRYPTERES. VALIDERES, OG HENTER PUBLIC KEY VIA API ***");
			byte[] byteContent;
            try {
	            byteContent = IOUtils.toByteArray(unencryptetContent);
            } catch (IOException e) {
	            throw new DigipostClientException(GENERAL_ERROR, "Unable to read content of document with uuid " + document.uuid, e);
            }
			documentsPreparer.validateAndSetNrOfPages(message.getChannel(), document, byteContent, Apply.partially(resolvePdfValidationSettings).of(message));
			InputStream encryptetContent = fetchKeyAndEncrypt(document, new ByteArrayInputStream(byteContent));
			delivery = uploadContent(message, document, encryptetContent);
		} else {
			delivery = uploadContent(message, document, unencryptetContent);
		}
		return delivery;
	}


	public MessageDelivery sendMessage(final MessageDelivery message) {
		MessageDelivery deliveredMessage = null;
		if (message.isAlreadyDeliveredToDigipost()) {
			log("\n\n---BREVET ER ALLEREDE SENDT");
		} else if (message.getSendLink() == null) {
			log("\n\n---BREVET ER IKKE KOMPLETT, KAN IKKE SENDE");
		} else {
			deliveredMessage = send(message);
		}
		return deliveredMessage;
	}



	/**
	 * Henter brukers public nøkkel fra serveren og krypterer brevet som skal
	 * sendes med denne.
	 */
	public InputStream fetchKeyAndEncrypt(Document document, InputStream content) {
		checkThatMessageCanBePreEncrypted(document);

		try(CloseableHttpResponse encryptionKeyResponse = apiService.getEncryptionKey(document.getEncryptionKeyLink().getUri())){
			checkResponse(encryptionKeyResponse);

			EncryptionKey key = unmarshal(encryptionKeyContext, encryptionKeyResponse.getEntity().getContent(), EncryptionKey.class);
			return the(new DigipostPublicKey(key)).map(keyToEncrypter).orElse(FAIL_IF_TRYING_TO_ENCRYPT).encrypt(content);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	public IdentificationResultWithEncryptionKey identifyAndGetEncryptionKey(Identification identification) {
		try(CloseableHttpResponse response = apiService.identifyAndGetEncryptionKey(identification)){
			checkResponse(response);
			IdentificationResultWithEncryptionKey result =
					unmarshal(identificationResultWithEncryptionKeyContext, response.getEntity().getContent(), IdentificationResultWithEncryptionKey.class);
			if (result.getResult().getResult() == IdentificationResultCode.DIGIPOST) {
				if (result.getEncryptionKey() == null) {
					throw new DigipostClientException(ErrorCode.SERVER_ERROR, "Server identifisert mottaker som Digipost-bruker, men sendte ikke med krypteringsnøkkel. Indikerer en feil hos Digipost.");
				}
				log("Mottaker er Digipost-bruker. Hentet krypteringsnøkkel.");
			} else {
				log("Mottaker er ikke Digipost-bruker.");
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public DigipostPublicKey getEncryptionKeyForPrint() {
		DateTime now = DateTime.now();

		if (!digipostClientConfig.cachePrintKey || (printKeyCachedTime == null || new Duration(printKeyCachedTime, now).isLongerThan(Duration.standardMinutes(5)))) {
			log("*** STARTER INTERAKSJON MED API: HENT KRYPTERINGSNØKKEL FOR PRINT ***");
			try(CloseableHttpResponse response = apiService.getEncryptionKeyForPrint()){
				checkResponse(response);
				EncryptionKey encryptionKey = unmarshal(encryptionKeyContext, response.getEntity().getContent(), EncryptionKey.class);
				cachedPrintKey = new DigipostPublicKey(encryptionKey);
				printKeyCachedTime = now;
				return cachedPrintKey;
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			log("Bruker cachet krypteringsnøkkel for print");
			return cachedPrintKey;
		}
	}


	private MessageDelivery uploadContent(MessageDelivery createdMessage, Document document, InputStream documentContent) {
        log("*** STARTER INTERAKSJON MED API: LEGGE TIL FIL ***");

		try(CloseableHttpResponse response = apiService.addContent(document, documentContent)){

        	checkResponse(response);

        	log("Innhold ble lagt til. Status: [" + response + "]");

			return unmarshal(messageDeliveryContext, response.getEntity().getContent(), MessageDelivery.class);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}



	/**
	 * Sender en forsendelse. For at denne metoden skal kunne kalles, må man
	 * først ha lagt innhold til forsendelsen med {@code addContent}.
	 */
	private MessageDelivery send(final MessageDelivery delivery) {
		log("*** STARTER INTERAKSJON MED API: SENDER MELDING MED ID " + delivery.getMessageId() + " ***");
		try(CloseableHttpResponse response = apiService.send(delivery)){

			checkResponse(response);

			log("Brevet ble sendt. Status: [" + response.toString() + "]");

			MessageDelivery messageDelivery = unmarshal(messageDeliveryContext, response.getEntity().getContent(), MessageDelivery.class);
			return messageDelivery;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void checkThatMessageHasNotAlreadyBeenDelivered(final MessageDelivery existingMessage) {
		switch (existingMessage.getStatus()) {
		case DELIVERED: {
			String errorMessage = String.format("En forsendelse med samme id=[%s] er allerede levert til mottaker den [%s]. "
					+ "Dette skyldes sannsynligvis doble kall til Digipost.", existingMessage.getMessageId(),
					existingMessage.getDeliveryTime());
			log(errorMessage);
			throw new DigipostClientException(ErrorCode.DIGIPOST_MESSAGE_ALREADY_DELIVERED, errorMessage);
		}
		case DELIVERED_TO_PRINT: {
			String errorMessage = String.format("En forsendelse med samme id=[%s] er allerede levert til print den [%s]. "
					+ "Dette skyldes sannsynligvis doble kall til Digipost.", existingMessage.getMessageId(),
					existingMessage.getDeliveryTime());
			log(errorMessage);
			throw new DigipostClientException(ErrorCode.PRINT_MESSAGE_ALREADY_DELIVERED, errorMessage);
		}
		default:
			break;
		}
	}

	private void checkThatMessageCanBePreEncrypted(final Document document) {
		Link encryptionKeyLink = document.getEncryptionKeyLink();
		if (encryptionKeyLink == null) {
			String errorMessage = "Document med id [" + document.uuid + "] kan ikke prekrypteres.";
			log(errorMessage);
			throw new DigipostClientException(ErrorCode.CANNOT_PREENCRYPT, errorMessage);
		}
	}

	private void verifyCorrectStatus(final MessageDelivery createdMessage, final MessageStatus expectedStatus) {
		if (createdMessage.getStatus() != expectedStatus) {
			throw new DigipostClientException(ErrorCode.INVALID_TRANSACTION,
					"Kan ikke legge til innhold til en forsendelse som ikke er i tilstanden " + expectedStatus + ".");
		}
	}


	private EncryptionKeyAndDocsWithInputstream fetchEncryptionKeyForRecipientIfNecessaryAndMapContentToInputstream(Message message,
																						Map<String, DocumentContent> documentsAndContent) {
		final Map<Document, InputStream> documentsAndInputstream = new LinkedHashMap<>();
		Optional<DigipostPublicKey> publicKeys = none();
		Message singleChannelMessage;

			if (message.isDirectPrint()) {
				singleChannelMessage = setMapAndMessageToPrint(message, documentsAndContent, documentsAndInputstream);

				if (singleChannelMessage.hasAnyDocumentRequiringPreEncryption()) {
					eventLogger.log("Direkte print. Bruker krypteringsnøkkel for print.");
					publicKeys = optional(getEncryptionKeyForPrint());
				}

			} else if (!message.recipient.hasPrintDetails() && !message.hasAnyDocumentRequiringPreEncryption()) {
				singleChannelMessage = setMapAndMessageToDigipost(message, documentsAndContent, documentsAndInputstream);

			} else {
				IdentificationResultWithEncryptionKey result = identifyAndGetEncryptionKey(message.recipient.toIdentification());
				if (result.getResultCode() == IdentificationResultCode.DIGIPOST) {
					singleChannelMessage = setMapAndMessageToDigipost(message, documentsAndContent, documentsAndInputstream);

					if (singleChannelMessage.hasAnyDocumentRequiringPreEncryption()) {
						eventLogger.log("Mottaker er Digipost-bruker. Bruker brukers krypteringsnøkkel.");
						publicKeys = optional(new DigipostPublicKey(result.getEncryptionKey()));
					}
				} else if (message.recipient.hasPrintDetails()) {
					singleChannelMessage = setMapAndMessageToPrint(message, documentsAndContent, documentsAndInputstream);

					if (singleChannelMessage.hasAnyDocumentRequiringPreEncryption()) {
						eventLogger.log("Mottaker er ikke Digipost-bruker. Bruker krypteringsnøkkel for print.");
						publicKeys = optional(getEncryptionKeyForPrint());
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
										   Map<Document, InputStream> documentsAndInputStream){
		Message singleChannelMessage = Message.copyMessageWithOnlyPrintDetails(messageToCopy);
		setPrintContentToUUID(documentsAndContent, documentsAndInputStream, singleChannelMessage.getAllDocuments());

		return singleChannelMessage;
	}

	static void setDigipostContentToUUID(Map<String, DocumentContent> documentsAndContent, Map<Document, InputStream> documentsAndInputstream, List<Document> allDocuments) {
		for(Document doc : allDocuments) {
			documentsAndInputstream.put(doc, documentsAndContent.get(doc.uuid).getDigipostContent());
		}
	}

	static void setPrintContentToUUID(Map<String, DocumentContent> documentsAndContent, Map<Document, InputStream> documentsAndInputstream, List<Document> allDocuments) {
		for(Document doc : allDocuments) {
			documentsAndInputstream.put(doc, documentsAndContent.get(doc.uuid).getPrintContent());
		}
	}

	private static URI responseToURI(CloseableHttpResponse response){
		try {
			return new URI(response.getFirstHeader("location").getValue());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static class EncryptionKeyAndDocsWithInputstream{
		public final Optional<DigipostPublicKey> digipostPublicKeys;
		public final Map<Document, InputStream> documentsAndInputstream;
		private final Message singleChannelMessage;

		public EncryptionKeyAndDocsWithInputstream(Optional<DigipostPublicKey> digipostPublicKeys,
							   Map<Document, InputStream> documentsAndInputstream, Message singleChannelMessage){
			this.digipostPublicKeys = digipostPublicKeys;
			this.documentsAndInputstream = documentsAndInputstream;
			this.singleChannelMessage = singleChannelMessage;
		}

		public Message getSingleChannelMessage(){
			return singleChannelMessage;
		}
	}

}
