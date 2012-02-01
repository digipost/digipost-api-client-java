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

import java.io.FileInputStream;
import java.io.InputStream;

import no.digipost.api.client.DigipostClientException.ErrorType;
import no.digipost.api.client.representations.ContentType;
import no.digipost.api.client.representations.print.PrintMessage;
import no.digipost.api.client.representations.print.PrintMessageStatus;

import com.sun.jersey.api.client.ClientResponse;

public class PrintOrderer extends Communicator {

	public PrintOrderer(final ApiService apiService, final EventLogger eventLogger) {
		super(apiService, eventLogger);
	}

	public PrintMessage orderPrint(final PrintMessage letterToPrint, final FileInputStream letterContent) {
		InputStream content = letterContent;

		log("\n\n---STARTER INTERAKSJON MED API: OPPRETTER Printforsendelse---");
		PrintMessage createdMessage = createOrFetchMessage(letterToPrint);

		if (createdMessage.skalPrekrypteres()) {
			log("\n\n---PRINTFORSENDELSE SKAL PREKRYPTERES, STARTER INTERAKSJON MED API: HENT PUBLIC KEY---");
			content = fetchKeyAndEncrypt(createdMessage, letterContent);
		}

		log("\n\n---STARTER INTERAKSJON MED API: LEGGE TIL FIL---");
		PrintMessage sentMessage = addToContentAndSendMessage(createdMessage, content);
		log("\n\n---API-INTERAKSJON ER FULLFØRT (OG PRINT AV BREVET ER DERMED BESTILT)---");
		return sentMessage;
	}

	public PrintMessage createOrFetchMessage(final PrintMessage message) {
		ClientResponse response = apiService.createPrintMessage(message);

		if (messageAlreadyExists(response)) {
			ClientResponse existingMessageResponse = apiService.fetchExistingMessage(response.getLocation());
			checkResponse(existingMessageResponse);
			PrintMessage exisitingMessage = existingMessageResponse.getEntity(PrintMessage.class);
			checkThatExistingMessageIsIdenticalToNewMessage(exisitingMessage, message);
			checkThatMessageIsReadyForContentUpload(exisitingMessage);
			log("Identisk printforsendelse fantes fra før. Bruker denne istedenfor å opprette ny. Status: [" + response.toString() + "]");
			return exisitingMessage;
		} else {
			checkResponse(response);
			log("Forsendelse opprettet. Status: [" + response.toString() + "]");
			return response.getEntity(PrintMessage.class);
		}
	}

	public PrintMessage addToContentAndSendMessage(final PrintMessage createdMessage, final InputStream letterContent) {
		verifyCorrectStatus(createdMessage, PrintMessageStatus.NOT_COMPLETE);
		ClientResponse response = apiService.addToContentAndSend(createdMessage, letterContent, ContentType.PDF);

		check404Error(response, ErrorType.MESSAGE_DOES_NOT_EXIST);
		checkResponse(response);

		log("Innhold ble lagt til og brevet sendt. Status: [" + response.toString() + "]");
		return response.getEntity(PrintMessage.class);
	}

	private void checkThatMessageIsReadyForContentUpload(final PrintMessage existingMessage) {
		if (PrintMessageStatus.NOT_COMPLETE != existingMessage.getStatus()) {
			String errorMessage = "Print av en identisk forsendelse er allerede bestilt. Dette skyldes sannsynligvis doble kall til Digipost.";
			log(errorMessage);
			throw new DigipostClientException(ErrorType.MESSAGE_ALREADY_DELIVERED, errorMessage);
		}
	}

	protected void verifyCorrectStatus(final PrintMessage createdMessage, final PrintMessageStatus expectedStatus) {
		if (createdMessage.getStatus() != expectedStatus) {
			throw new DigipostClientException(ErrorType.INVALID_TRANSACTION,
					"Kan ikke legge til innhold til en forsendelse som ikke er i tilstanden " + expectedStatus + ".");
		}
	}

}
