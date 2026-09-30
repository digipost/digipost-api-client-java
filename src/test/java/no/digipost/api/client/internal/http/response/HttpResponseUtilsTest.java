/*
 * Copyright (C) Posten Bring AS
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
package no.digipost.api.client.internal.http.response;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.ErrorType;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpResponseUtilsTest {

    private static final EventLogger NO_OP_EVENT_LOGGER = EventLogger.NOOP_LOGGER;

    @Test
    void tooManyRequestsResolvesToDedicatedErrorCode() {
        ClassicHttpResponse response = Mockito.mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_TOO_MANY_REQUESTS);
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        marshal(jaxbContext, new ErrorMessage(ErrorType.CLIENT_TECHNICAL, "Rate limit exceeded"), body);
        when(response.getEntity()).thenReturn(new ByteArrayEntity(body.toByteArray(), ContentType.APPLICATION_XML));

        DigipostClientException exception = assertThrows(DigipostClientException.class,
                () -> HttpResponseUtils.checkResponse(response, NO_OP_EVENT_LOGGER));

        assertThat(exception.getErrorCode(), is(ErrorCode.TOO_MANY_REQUESTS));
    }

    @Test
    void tooManyRequestsWithoutResponseBodyStillResolvesToDedicatedErrorCode() {
        ClassicHttpResponse response = Mockito.mock(ClassicHttpResponse.class);
        when(response.getCode()).thenReturn(HttpStatus.SC_TOO_MANY_REQUESTS);
        when(response.getEntity()).thenReturn(null);

        DigipostClientException exception = assertThrows(DigipostClientException.class,
                () -> HttpResponseUtils.checkResponse(response, NO_OP_EVENT_LOGGER));

        assertThat(exception.getErrorCode(), is(ErrorCode.TOO_MANY_REQUESTS));
    }
}
