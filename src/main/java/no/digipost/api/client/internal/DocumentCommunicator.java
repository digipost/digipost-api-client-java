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
package no.digipost.api.client.internal;

import no.digipost.api.client.ApiService;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.representations.DocumentStatus;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.util.HttpClientUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;

import static no.digipost.api.client.util.HttpClientUtils.checkResponse;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;

public class DocumentCommunicator  {

    private final ApiService apiService;
    private final EventLogger eventLogger;

    public DocumentCommunicator(final ApiService apiService, final EventLogger eventLogger) {
        this.apiService = apiService;
        this.eventLogger = eventLogger;
    }

    public DocumentEvents getDocumentEvents(String organisation, String partId, ZonedDateTime from, ZonedDateTime to, int offset, int maxResults) {
        try(CloseableHttpResponse response = apiService.getDocumentEvents(organisation, partId, from, to, offset, maxResults)){;
            checkResponse(response, eventLogger);
            return unmarshal(jaxbContext, response.getEntity().getContent(), DocumentEvents.class);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public InputStream getContent(String path) {
        return HttpClientUtils.safelyOfferEntityStreamExternally(apiService.getContent(path), eventLogger);
    }

    public DocumentStatus getDocumentStatus(Link linkToDocumentStatus) {
        try(CloseableHttpResponse response = apiService.getDocumentStatus(linkToDocumentStatus)){
            checkResponse(response, eventLogger);
            return unmarshal(jaxbContext, response.getEntity().getContent(), DocumentStatus.class);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public DocumentStatus getDocumentStatus(long senderId, String uuid) {
        try(CloseableHttpResponse response = apiService.getDocumentStatus(senderId, uuid)) {
            checkResponse(response, eventLogger);
            return unmarshal(jaxbContext, response.getEntity().getContent(), DocumentStatus.class);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
