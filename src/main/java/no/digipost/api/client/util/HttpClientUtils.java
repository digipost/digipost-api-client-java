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

import no.digipost.api.client.Communicator;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.errorhandling.DigipostClientException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;

import static no.digipost.api.client.errorhandling.ErrorCode.GENERAL_ERROR;

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
            Communicator.checkResponse(response, eventLogger);
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

    private HttpClientUtils() {}
}
