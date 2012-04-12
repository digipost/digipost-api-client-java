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
import no.digipost.api.client.representations.MessageStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;

public class MessageSender extends Communicator {
	static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);

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
	public Message sendMessage(final Message message, final InputStream letterContent, final ContentType contentType) {
		InputStream content = letterContent;

		log("\n\n---STARTER INTERAKSJON MED API: OPPRETTE FORSENDELSE---");
		Message createdMessage = createOrFetchMessage(message);

		if (message.skalPrekrypteres()) {
			log("\n\n---FORSENDELSE SKAL PREKRYPTERES, STARTER INTERAKSJON MED API: HENT PUBLIC KEY---");
			content = fetchKeyAndEncrypt(createdMessage, letterContent);
		}

		log("\n\n---STARTER INTERAKSJON MED API: LEGGE TIL FIL---");
		Message sentMessage = addToContentAndSendMessage(createdMessage, content, contentType);
		log("\n\n---API-INTERAKSJON ER FULLFØRT (OG BREVET ER DERMED SENDT)---");
		return sentMessage;
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
	public Message createOrFetchMessage(final Message message) {
		ClientResponse response = apiService.createMessage(message);

		if (messageAlreadyExists(response)) {

			ClientResponse existingMessageResponse = apiService.fetchExistingMessage(response.getLocation());
			checkResponse(existingMessageResponse);
			Message exisitingMessage = existingMessageResponse.getEntity(Message.class);
			checkThatExistingMessageIsIdenticalToNewMessage(exisitingMessage, message);
			checkThatMessageHasNotAlreadyBeenDelivered(exisitingMessage);
			log("Identisk forsendelse fantes fra før. Bruker denne istedenfor å opprette ny. Status: [" + response.toString() + "]");
			return exisitingMessage;

		} else {

			check404Error(response, ErrorType.RECIPIENT_DOES_NOT_EXIST);
			checkResponse(response);
			log("Forsendelse opprettet. Status: [" + response.toString() + "]");
			return response.getEntity(Message.class);

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
	public Message addToContentAndSendMessage(final Message createdMessage, final InputStream letterContent, final ContentType contentType) {
		verifyCorrectStatus(createdMessage, MessageStatus.NOT_COMPLETE);
		ClientResponse response = apiService.addToContentAndSend(createdMessage, letterContent, contentType);

		check404Error(response, ErrorType.MESSAGE_DOES_NOT_EXIST);
		checkResponse(response);

		log("Innhold ble lagt til og brevet sendt. Status: [" + response.toString() + "]");
		return response.getEntity(Message.class);
	}

	private void checkThatMessageHasNotAlreadyBeenDelivered(final Message existingMessage) {
		if (MessageStatus.DELIVERED == existingMessage.getStatus()) {
			String errorMessage = "En identisk forsendelse er allerede levert til mottaker. Dette skyldes sannsynligvis doble kall til Digipost.";
			log(errorMessage);
			throw new DigipostClientException(ErrorType.DIGIPOST_MESSAGE_ALREADY_DELIVERED, errorMessage);
		}
	}

	protected void verifyCorrectStatus(final Message createdMessage, final MessageStatus expectedStatus) {
		if (createdMessage.getStatus() != expectedStatus) {
			throw new DigipostClientException(ErrorType.INVALID_TRANSACTION,
					"Kan ikke legge til innhold til en forsendelse som ikke er i tilstanden " + expectedStatus + ".");
		}
	}
}
