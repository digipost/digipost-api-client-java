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
import no.digipost.api.client.representations.ErrorMessage;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.IOException;

import static no.digipost.api.client.representations.ErrorType.SERVER;

public class ApiCommons {

	private static final EventLogger NOOP_EVENTLOGGER = new EventLogger() {
		@Override
		public void log(final String logMessage) {

		}
	};
	static final Logger LOG = LoggerFactory.getLogger(ApiCommons.class);

	public static void checkResponse(final CloseableHttpResponse response) {
		checkResponse(response, NOOP_EVENTLOGGER);
	}

	public static void checkResponse(final CloseableHttpResponse response, EventLogger eventLogger) {
		int status = response.getStatusLine().getStatusCode();
		if (!responseOk(status)) {
			ErrorMessage error = fetchErrorMessageString(response);
			log(error.toString(), eventLogger);
			switch (status) {
				case HttpStatus.SC_INTERNAL_SERVER_ERROR:
					throw new DigipostClientException(ErrorCode.SERVER_ERROR, error.getErrorMessage());
				case HttpStatus.SC_SERVICE_UNAVAILABLE:
					throw new DigipostClientException(ErrorCode.API_UNAVAILABLE, error.getErrorMessage());
				default:
					throw new DigipostClientException(error);
			}
		}
	}

	private static boolean responseOk(final int status) {
		switch (status) {
		case HttpStatus.SC_CREATED:
		case HttpStatus.SC_OK:
			return true;
		default:
			return false;
		}
	}

	protected static ErrorMessage fetchErrorMessageString(final CloseableHttpResponse response) {
		try {
			final String body = EntityUtils.toString(response.getEntity());
			try {
				ErrorMessage errorMessage = JAXB.unmarshal(response.getEntity().getContent(), ErrorMessage.class);
				return errorMessage != null ? errorMessage : ErrorMessage.EMPTY;
			} catch (IllegalStateException | DataBindingException e) {
				return new ErrorMessage(SERVER, ErrorCode.SERVER_ERROR.name(),
						"Error reading response: " + e.getClass().getName() + " - " + e.getMessage() + ", body entity was: "
								+ body);
			} finally {
				response.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected static void log(final String message, EventLogger logger) {
		LOG.debug(message);
		logger.log(message);
	}
}
