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
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Superklasse for MessageSender som har funksjonalitet for å snakke med
 * ApiService.
 *
 */
public abstract class Communicator {

	protected final EventLogger eventLogger;
	protected final ApiService apiService;

	public Communicator(final ApiService apiService, final EventLogger eventLogger) {
		this.apiService = apiService;
		this.eventLogger = eventLogger;
	}

	protected void checkResponse(CloseableHttpResponse response) {
		ApiCommons.checkResponse(response, eventLogger);
	}

	protected void log(final String message) {
		ApiCommons.log(message, eventLogger);
	}

	protected void logThrowable(final Throwable t) {
		ApiCommons.LOG.debug("Feil.", t);

		StringWriter stacktrace = new StringWriter();
		t.printStackTrace(new PrintWriter(stacktrace));
		eventLogger.log(stacktrace.toString());
	}

	protected boolean resourceAlreadyExists(final CloseableHttpResponse response) {
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT;
	}

	protected void checkThatExistingMessageIsIdenticalToNewMessage(final MessageDelivery exisitingMessage, final Message message) {
		if (!exisitingMessage.isSameMessageAs(message)) {
			String errorMessage = "Forsendelse med id [" + message.messageId + "] finnes fra før med annen spesifikasjon.";
			ApiCommons.log(errorMessage, eventLogger);
			throw new DigipostClientException(ErrorCode.DUPLICATE_MESSAGE, errorMessage);
		}
	}


}