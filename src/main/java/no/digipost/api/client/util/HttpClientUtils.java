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
package no.digipost.api.client.util;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.ErrorType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import javax.xml.bind.DataBindingException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static no.digipost.api.client.errorhandling.ErrorCode.GENERAL_ERROR;
import static no.digipost.api.client.util.ExceptionUtils.exceptionNameAndMessage;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;

public final class HttpClientUtils {

    /**
     * Do proper resource handling in the case when offering the {@link InputStream stream} from a http response
     * to a third party. The third party must be expected to do proper resource handling on the received stream, but
     * in the case of any thrown exception when acquiring the stream, the 3rd party has no way of closing the resources
     * of the response, and thus must be handled by the method returning the stream.
     *
     * @param response the response to acquire the entity stream from
     *
     * @return the stream containing the entity of the response.
     */
    public static InputStream safelyOfferEntityStreamExternally(CloseableHttpResponse response, EventLogger eventLogger) {
        HttpEntity entity = null;
        try {
            HttpClientUtils.checkResponse(response, eventLogger);
            entity = response.getEntity();
            return entity.getContent();
        } catch (IOException | RuntimeException e) {
            try (CloseableHttpResponse autoClosed = response) {
                EntityUtils.consume(entity);
            } catch (IOException | RuntimeException entityConsumptionException) {
                e.addSuppressed(entityConsumptionException);
            }
            throw e instanceof DigipostClientException ? (DigipostClientException) e : new DigipostClientException(GENERAL_ERROR, e.getMessage(), e);
        }
    }

    public static boolean responseOk(StatusLine status) {
        return status.getStatusCode() / 100 == 2; //all 2xx is ok
    }

    public static boolean resourceAlreadyExists(CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT;
    }


    public static void checkResponse(final CloseableHttpResponse response, EventLogger eventLogger) {
        StatusLine status = response.getStatusLine();
        if (!responseOk(status)) {
            ErrorMessage error = fetchErrorMessageString(status, response.getEntity());
            eventLogger.log(error.toString());
            switch (status.getStatusCode()) {
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    throw new DigipostClientException(ErrorCode.SERVER_ERROR, error.getErrorMessage());
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    throw new DigipostClientException(ErrorCode.API_UNAVAILABLE, error.getErrorMessage());
                default:
                    throw new DigipostClientException(error);
            }
        }
    }

    private static ErrorMessage fetchErrorMessageString(final StatusLine statusLine, final HttpEntity responseEntity) {
        final ErrorType errorType = ErrorType.fromResponseStatus(statusLine);
        if (responseEntity == null) {
            return new ErrorMessage(errorType, "status=" + statusLine + ", body=<empty>");
        }

        final byte[] responseContent;
        try {
            responseContent = EntityUtils.toByteArray(responseEntity);
        } catch (IOException e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR,
                    "status=" + statusLine + ", clientException=" + exceptionNameAndMessage(e), e);
        }
        if (responseContent.length == 0) {
            return new ErrorMessage(errorType, "status=" + statusLine + ", body=<empty>");
        }

        try {
            ErrorMessage errorMessage = unmarshal(jaxbContext, new ByteArrayInputStream(responseContent), ErrorMessage.class);
            return errorMessage != null ? errorMessage.withMessage("status=" + statusLine + ", message=" + errorMessage.getErrorMessage()) : ErrorMessage.EMPTY;
        } catch (IllegalStateException | DataBindingException e) {
            return new ErrorMessage(errorType, errorType.name(),
                    "status=" + statusLine + ", clientException=" + exceptionNameAndMessage(e) + ", body=" + new String(responseContent));
        } catch (Exception e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR,
                    "status=" + statusLine + ", clientException=" + exceptionNameAndMessage(e) + ", body=" + new String(responseContent), e);
        }
    }

    private HttpClientUtils() {}
}
