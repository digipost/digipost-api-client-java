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
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class MessageSender extends Communicator {

	public MessageSender(final ApiService apiService, final EventLogger eventLogger) {
		super(apiService, eventLogger);
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

	private MessageDelivery uploadContent(final MessageDelivery createdMessage, final Document document,
										  final InputStream documentContent) {
		log("\n\n---STARTER INTERAKSJON MED API: LEGGE TIL FIL---");

		Response response = apiService.addContent(document, documentContent);

		checkResponse(response);

		log("Innhold ble lagt til. Status: [" + response.toString() + "]");
		return response.readEntity(createdMessage.getClass());
	}

	public MessageDelivery createMultipartMessage(final MultiPart multiPart) {
		Response response = apiService.multipartMessage(multiPart);
		checkResponse(response);

		log("Brevet ble sendt. Status: [" + response.toString() + "]");
		return response.readEntity(MessageDelivery.class);
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
			log("\n\n---DOKUMENTET SKAL PREKRYPTERES, STARTER INTERAKSJON MED API: HENT PUBLIC KEY---");
			final InputStream encryptetContent = fetchKeyAndEncrypt(document, unencryptetContent);
			delivery = uploadContent(message, document, encryptetContent);
		} else {
			delivery = uploadContent(message, document, unencryptetContent);
		}
		return delivery;
	}

	/**
	 * Sender en forsendelse. For at denne metoden skal kunne kalles, må man
	 * først ha lagt innhold til forsendelsen med {@code addContent}.
	 */
	private MessageDelivery send(final MessageDelivery delivery) {
		Response response = apiService.send(delivery);

		checkResponse(response);

		log("Brevet ble sendt. Status: [" + response.toString() + "]");
		return response.readEntity(delivery.getClass());
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

	protected void verifyCorrectStatus(final MessageDelivery createdMessage, final MessageStatus expectedStatus) {
		if (createdMessage.getStatus() != expectedStatus) {
			throw new DigipostClientException(ErrorCode.INVALID_TRANSACTION,
					"Kan ikke legge til innhold til en forsendelse som ikke er i tilstanden " + expectedStatus + ".");
		}
	}
}
