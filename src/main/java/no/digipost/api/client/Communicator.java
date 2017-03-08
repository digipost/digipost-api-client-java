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
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DataBindingException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static no.digipost.api.client.representations.ErrorType.SERVER;
import static no.digipost.api.client.util.JAXBContextUtils.errorMessageContext;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;

/**
 * Superklasse for MessageSender som har funksjonalitet for å snakke med
 * ApiService.
 */
public abstract class Communicator {

    private static final Logger LOG = LoggerFactory.getLogger(Communicator.class);

    protected final EventLogger eventLogger;
    protected final ApiService apiService;

    public Communicator(final ApiService apiService, final EventLogger eventLogger) {
        this.apiService = apiService;
        this.eventLogger = eventLogger;
    }

    protected void checkResponse(CloseableHttpResponse response) {
        checkResponse(response, eventLogger);
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

    protected static ErrorMessage fetchErrorMessageString(final CloseableHttpResponse response) {
        try {
            ErrorMessage errorMessage = unmarshal(errorMessageContext, response.getEntity().getContent(), ErrorMessage.class);
            response.close();
            return errorMessage != null ? errorMessage : ErrorMessage.EMPTY;
        } catch (IllegalStateException | DataBindingException e) {
            return new ErrorMessage(SERVER, ErrorCode.SERVER_ERROR.name(),
                    e.getClass().getSimpleName() + ": Det skjedde en feil på serveren (" + e.getMessage() +
                            "), men klienten kunne ikke lese responsen.");
        } catch (IOException e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e);
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

    protected void log(final String message) {
        log(message, eventLogger);
    }

    protected static void log(final String message, EventLogger logger) {
        LOG.debug(message);
        logger.log(message);
    }

    protected void logThrowable(final Throwable t) {
        LOG.debug("Feil.", t);

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
            log(errorMessage, eventLogger);
            throw new DigipostClientException(ErrorCode.DUPLICATE_MESSAGE, errorMessage);
        }
    }

}
