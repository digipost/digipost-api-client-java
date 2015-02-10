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
import no.digipost.api.client.representations.*;
import no.digipost.api.client.util.DigipostPublicKey;
import no.digipost.api.client.util.Encrypter;
import no.digipost.print.validate.PdfValidationSettings;
import no.digipost.print.validate.PdfValidator;
import no.motif.single.Optional;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import static no.digipost.api.client.errorhandling.ErrorCode.GENERAL_ERROR;
import static no.motif.Singular.none;
import static no.motif.Singular.optional;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class MessageSender extends Communicator {

	private final DocumentsPreparer documentsPreparer;

	private DateTime printKeyCachedTime = null;
	private DigipostPublicKey cachedPrintKey;


	public MessageSender(ApiService apiService, EventLogger eventLogger, PdfValidator pdfValidator) {
		super(apiService, eventLogger);
		this.documentsPreparer = new DocumentsPreparer(pdfValidator);
	}


	public void setPdfValidationSettings(PdfValidationSettings settings) {
		this.documentsPreparer.setPdfValidationSettings(settings);
	}


	/**
	 * Sender melding med alle dokumenter og innhold med én API-forespørsel (HTTP multipart request).
	 * Dersom dokumentene skal direkte til print og skal prekrypteres før sending kan det gjøres en ekstra request for å hente
	 * krypteringsnøkkel.
	 */
	public MessageDelivery sendMultipartMessage(Message message, Map<Document, InputStream> documentsAndContent) {
		Optional<DigipostPublicKey> krypteringsnokkel = fetchEncryptionKeyForRecipientIfNecessary(message);

		try (MultiPart multiPart = new MultiPart()) {
			BodyPart messageBodyPart = new BodyPart(message, MediaType.valueOf(MediaTypes.DIGIPOST_MEDIA_TYPE_V6));
			ContentDisposition messagePart = ContentDisposition.type("attachment").fileName("message").build();
			messageBodyPart.setContentDisposition(messagePart);
			multiPart.bodyPart(messageBodyPart);


			for (Entry<Document, InputStream> documentAndContent : documentsPreparer.prepare(documentsAndContent, message, krypteringsnokkel).entrySet()) {
				Document document = documentAndContent.getKey();
				InputStream content = documentAndContent.getValue();
				BodyPart bodyPart = new BodyPart(content, new MediaType("application", defaultIfBlank(document.getDigipostFileType(), "octet-stream")));
				ContentDisposition documentPart = ContentDisposition.type("attachment").fileName(document.uuid).build();
				bodyPart.setContentDisposition(documentPart);
				multiPart.bodyPart(bodyPart);
			}
			log("*** STARTER INTERAKSJON MED API: SENDER MELDING MED ID " + message.messageId + " ***");
			Response response = apiService.multipartMessage(multiPart);
			checkResponse(response);

			log("Brevet ble sendt. Status: [" + response + "]");
			return response.readEntity(MessageDelivery.class);

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
		Response response = apiService.createMessage(message);

		if (resourceAlreadyExists(response)) {
			Response existingMessageResponse = apiService.fetchExistingMessage(response.getLocation());
			checkResponse(existingMessageResponse);
			MessageDelivery delivery = existingMessageResponse.readEntity(MessageDelivery.class);
			checkThatExistingMessageIsIdenticalToNewMessage(delivery, message);
			checkThatMessageHasNotAlreadyBeenDelivered(delivery);
			log("Identisk forsendelse fantes fra før. Bruker denne istedenfor å opprette ny. Status: [" + response.toString() + "]");
			return delivery;
		} else {
			checkResponse(response);
			log("Forsendelse opprettet. Status: [" + response.toString() + "]");
			return response.readEntity(MessageDelivery.class);
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
			log("*** DOKUMENTET SKAL PREKRYPTERES, STARTER INTERAKSJON MED API: HENT PUBLIC KEY ***");
			byte[] byteContent;
            try {
	            byteContent = IOUtils.toByteArray(unencryptetContent);
            } catch (IOException e) {
	            throw new DigipostClientException(GENERAL_ERROR, "Unable to read content of document with uuid " + document.uuid, e);
            }
			documentsPreparer.validate(message.getDeliveryMethod(), document, byteContent);
			InputStream encryptetContent = fetchKeyAndEncrypt(document, unencryptetContent);
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

		Response encryptionKeyResponse = apiService.getEncryptionKey(document.getEncryptionKeyLink().getUri());

		checkResponse(encryptionKeyResponse);

		EncryptionKey key = encryptionKeyResponse.readEntity(EncryptionKey.class);

		return Encrypter.encryptContent(content, new DigipostPublicKey(key));
	}

	public IdentificationResultWithEncryptionKey identifyAndGetEncryptionKey(Identification identification) {
		Response response = apiService.identifyAndGetEncryptionKey(identification);
		checkResponse(response);

		IdentificationResultWithEncryptionKey result = response.readEntity(IdentificationResultWithEncryptionKey.class);
		if (result.getResult().getResult() == IdentificationResultCode.DIGIPOST) {
			if (result.getEncryptionKey() == null) {
				throw new DigipostClientException(ErrorCode.SERVER_ERROR, "Server identifisert mottaker som Digipost-bruker, men sendte ikke med krypteringsnøkkel. Indikerer en feil hos Digipost.");
			}
			log("Mottaker er Digipost-bruker. Hentet krypteringsnøkkel.");
		} else {
			log("Mottaker er ikke Digipost-bruker.");
		}
		return result;
	}

	public DigipostPublicKey getEncryptionKeyForPrint() {
		DateTime now = DateTime.now();

		if (printKeyCachedTime == null || new Duration(printKeyCachedTime, now).isLongerThan(Duration.standardMinutes(5))) {
			log("*** STARTER INTERAKSJON MED API: HENT KRYPTERINGSNØKKEL FOR PRINT ***");
			Response response = apiService.getEncryptionKeyForPrint();
			checkResponse(response);
			EncryptionKey encryptionKey = response.readEntity(EncryptionKey.class);
			cachedPrintKey = new DigipostPublicKey(encryptionKey);
			printKeyCachedTime = now;
			return cachedPrintKey;
		} else {
			log("Bruker cachet krypteringsnøkkel for print");
			return cachedPrintKey;
		}
	}


	private MessageDelivery uploadContent(MessageDelivery createdMessage, Document document, InputStream documentContent) {
        log("*** STARTER INTERAKSJON MED API: LEGGE TIL FIL ***");

        Response response = apiService.addContent(document, documentContent);

        checkResponse(response);

        log("Innhold ble lagt til. Status: [" + response + "]");
        return response.readEntity(MessageDelivery.class);
	}



	/**
	 * Sender en forsendelse. For at denne metoden skal kunne kalles, må man
	 * først ha lagt innhold til forsendelsen med {@code addContent}.
	 */
	private MessageDelivery send(final MessageDelivery delivery) {
		log("*** STARTER INTERAKSJON MED API: SENDER MELDING MED ID " + delivery.getMessageId() + " ***");
		Response response = apiService.send(delivery);

		checkResponse(response);

		log("Brevet ble sendt. Status: [" + response.toString() + "]");
		return response.readEntity(MessageDelivery.class);
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


	private Optional<DigipostPublicKey> fetchEncryptionKeyForRecipientIfNecessary(Message message) {
		if (message.hasAnyDocumentRequiringPreEncryption()) {
			if (message.isDirectPrint()) {
				eventLogger.log("Direkte print. Bruker krypteringsnøkkel for print.");
				return optional(getEncryptionKeyForPrint());

			} else {
				IdentificationResultWithEncryptionKey result = identifyAndGetEncryptionKey(message.recipient.toIdentification());
				if (result.getResult().getResult() == IdentificationResultCode.DIGIPOST) {
					eventLogger.log("Mottaker er Digipost-bruker. Bruker brukers krypteringsnøkkel.");
					return optional(new DigipostPublicKey(result.getEncryptionKey()));
				} else if (message.recipient.hasPrintDetails()) {
					eventLogger.log("Mottaker er ikke Digipost-bruker. Bruker krypteringsnøkkel for print.");
					return optional(getEncryptionKeyForPrint());
				} else {
					throw new DigipostClientException(ErrorCode.UNKNOWN_RECIPIENT, "Mottaker er ikke Digipost-bruker og forsendelse mangler print-fallback.");
				}
			}
		}
		return none();
	}

}
