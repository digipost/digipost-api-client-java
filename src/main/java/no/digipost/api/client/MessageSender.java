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

import java.io.InputStream;

import no.digipost.api.client.representations.ContentType;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.MessageStatus;

import com.sun.jersey.api.client.ClientResponse;

public class MessageSender extends Communicator {

	public MessageSender(final ApiService apiService, final EventLogger eventLogger) {
		super(apiService, eventLogger);
	}

	/**
	 * Sender et brev gjennom Digipost. Denne metoden gjør alle HTTP-kallene som
	 * er nødvendige for å sende brevet. Det vil si at den først gjør et kall
	 * for å opprette en forsendelsesressurs på serveren og deretter poster
	 * brevets innhold. Hvis forsendelsen skal sendes ferdigkryptert fra
	 * klienten vil det gjøres et kall for å hente mottakers offentlige nøkkel
	 * (public key), for så å kryptere innholdet før det sendes over.
	 */
	public MessageDelivery createAndSendMessage(final Message message, final InputStream letterContent, final ContentType contentType) {
		return createAndSendMessage(message, letterContent, contentType, letterContent);
	}

	public MessageDelivery createAndSendMessage(final Message message, final InputStream letterContent, final ContentType contentType,
			final InputStream printContent) {
		log("\n\n---STARTER INTERAKSJON MED API: OPPRETTE FORSENDELSE---");
		MessageDelivery createdMessage = createOrFetchMessage(message);

		final InputStream unencryptetContent;
		final ContentType finalContentType;
		if (createdMessage.isDeliveredToDigipost()) {
			unencryptetContent = letterContent;
			finalContentType = contentType;
		} else {
			unencryptetContent = printContent;
			finalContentType = ContentType.PDF;
		}

		MessageDelivery delivery;
		if (message.isPreEncrypt()) {
			log("\n\n---FORSENDELSE SKAL PREKRYPTERES, STARTER INTERAKSJON MED API: HENT PUBLIC KEY---");
			final InputStream encryptetContent = fetchKeyAndEncrypt(createdMessage, unencryptetContent);
			delivery = uploadContentAndSend(finalContentType, createdMessage, encryptetContent);
		} else {
			delivery = uploadContentAndSend(finalContentType, createdMessage, unencryptetContent);
		}

		log("\n\n---API-INTERAKSJON ER FULLFØRT (OG BREVET ER DERMED SENDT)---");
		return delivery;
	}

	public MessageDelivery createMessageAndAddContent(final Message message, final InputStream letterContent,
			final ContentType contentType, final InputStream printContent) {
		log("\n\n---STARTER INTERAKSJON MED API: OPPRETTE FORSENDELSE---");
		MessageDelivery createdMessage = createOrFetchMessage(message);

		final InputStream unencryptetContent;
		final ContentType finalContentType;
		if (createdMessage.isDeliveredToDigipost()) {
			unencryptetContent = letterContent;
			finalContentType = contentType;
		} else {
			unencryptetContent = printContent;
			finalContentType = ContentType.PDF;
		}

		MessageDelivery delivery;
		if (message.isPreEncrypt()) {
			log("\n\n---FORSENDELSE SKAL PREKRYPTERES, STARTER INTERAKSJON MED API: HENT PUBLIC KEY---");
			final InputStream encryptetContent = fetchKeyAndEncrypt(createdMessage, unencryptetContent);
			delivery = uploadContent(finalContentType, createdMessage, encryptetContent);
		} else {
			delivery = uploadContent(finalContentType, createdMessage, unencryptetContent);
		}

		log("\n\n---API-INTERAKSJON ER FULLFØRT (OG BREVET ER DERMED OPPRETTET)---");
		return delivery;
	}

	public MessageDelivery sendMessage(final Message message) {
		MessageDelivery createdMessage = createOrFetchMessage(message);
		MessageDelivery deliveredMessage = null;
		if (createdMessage.isDeliveredToDigipost()) {
			log("\n\n---BREVET ER ALLEREDE SENDT");
		} else if (createdMessage.getSendLink() == null) {
			log("\n\n---BREVET ER IKKE KOMPLETT, KAN IKKE SENDE");
		} else {
			deliveredMessage = send(createdMessage);
		}
		return deliveredMessage;
	}

	private MessageDelivery uploadContentAndSend(final ContentType contentType, final MessageDelivery createdMessage,
			final InputStream unencryptetContent) {
		log("\n\n---STARTER INTERAKSJON MED API: LEGGE TIL FIL---");
		return addContentAndSendMessage(createdMessage, unencryptetContent, contentType);
	}

	private MessageDelivery uploadContent(final ContentType contentType, final MessageDelivery createdMessage,
			final InputStream unencryptetContent) {
		log("\n\n---STARTER INTERAKSJON MED API: LEGGE TIL FIL---");
		return addContent(createdMessage, unencryptetContent, contentType);
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
		ClientResponse response = apiService.createMessage(message);

		if (messageAlreadyExists(response)) {
			ClientResponse existingMessageResponse = apiService.fetchExistingMessage(response.getLocation());
			checkResponse(existingMessageResponse);
			MessageDelivery delivery = existingMessageResponse.getEntity(MessageDelivery.class);
			checkThatExistingMessageIsIdenticalToNewMessage(delivery, message);
			checkThatMessageHasNotAlreadyBeenDelivered(delivery);
			log("Identisk forsendelse fantes fra før. Bruker denne istedenfor å opprette ny. Status: [" + response.toString() + "]");
			return delivery;
		} else {
			check404Error(response, ErrorType.RECIPIENT_DOES_NOT_EXIST);
			checkResponse(response);
			log("Forsendelse opprettet. Status: [" + response.toString() + "]");
			return response.getEntity(MessageDelivery.class);
		}
	}

	/**
	 * Legger til innhold (PDF) til en forsendelse og sender brevet. For at
	 * denne metoden skal kunne kalles, må man først ha opprettet
	 * forsendelsesressursen på serveren ved metoden
	 * {@code createOrFetchMesssage}.
	 * 
	 * @param contentType
	 * 
	 */
	public MessageDelivery addContentAndSendMessage(final MessageDelivery delivery, final InputStream letterContent,
			final ContentType contentType) {
		verifyCorrectStatus(delivery, MessageStatus.NOT_COMPLETE);
		ClientResponse response = apiService.addContentAndSend(delivery, letterContent, contentType);

		check404Error(response, ErrorType.MESSAGE_DOES_NOT_EXIST);
		checkResponse(response);

		log("Innhold ble lagt til og brevet sendt. Status: [" + response.toString() + "]");
		return response.getEntity(delivery.getClass());
	}

	/**
	 * Legger til innhold (PDF) til en forsendelse. For at denne metoden skal
	 * kunne kalles, må man først ha opprettet forsendelsesressursen på serveren
	 * ved metoden {@code createOrFetchMesssage}.
	 * 
	 * @param contentType
	 * 
	 */
	public MessageDelivery addContent(final MessageDelivery delivery, final InputStream letterContent, final ContentType contentType) {
		verifyCorrectStatus(delivery, MessageStatus.NOT_COMPLETE);
		ClientResponse response = apiService.addContent(delivery, letterContent, contentType);

		check404Error(response, ErrorType.MESSAGE_DOES_NOT_EXIST);
		checkResponse(response);

		log("Innhold ble lagt til. Status: [" + response.toString() + "]");
		return response.getEntity(delivery.getClass());
	}

	/**
	 * Sender en forsendelse. For at denne metoden skal kunne kalles, må man
	 * først ha lagt innhold til forsendelsen med {@code TODO fyll inn navn}.
	 * 
	 * @param contentType
	 * 
	 */
	public MessageDelivery send(final MessageDelivery delivery) {
		verifyCorrectStatus(delivery, MessageStatus.COMPLETE);
		ClientResponse response = apiService.send(delivery);

		check404Error(response, ErrorType.MESSAGE_DOES_NOT_EXIST);
		checkResponse(response);

		log("Brevet ble sendt. Status: [" + response.toString() + "]");
		return response.getEntity(delivery.getClass());
	}

	private void checkThatMessageHasNotAlreadyBeenDelivered(final MessageDelivery existingMessage) {
		switch (existingMessage.getStatus()) {
		case DELIVERED: {
			String errorMessage = String.format("En forsendelse med samme id=[%s] er allerede levert til mottaker den [%s]. "
					+ "Dette skyldes sannsynligvis doble kall til Digipost.", existingMessage.getMessageId(),
					existingMessage.getDeliveredDate());
			log(errorMessage);
			throw new DigipostClientException(ErrorType.DIGIPOST_MESSAGE_ALREADY_DELIVERED, errorMessage);
		}
		case DELIVERED_TO_PRINT: {
			String errorMessage = String.format("En forsendelse med samme id=[%s] er allerede levert til print den [%s]. "
					+ "Dette skyldes sannsynligvis doble kall til Digipost.", existingMessage.getMessageId(),
					existingMessage.getDeliveredDate());
			log(errorMessage);
			throw new DigipostClientException(ErrorType.PRINT_MESSAGE_ALREADY_DELIVERED, errorMessage);
		}
		default:
			break;
		}
	}

	protected void verifyCorrectStatus(final MessageDelivery createdMessage, final MessageStatus expectedStatus) {
		if (createdMessage.getStatus() != expectedStatus) {
			throw new DigipostClientException(ErrorType.INVALID_TRANSACTION,
					"Kan ikke legge til innhold til en forsendelse som ikke er i tilstanden " + expectedStatus + ".");
		}
	}
}
