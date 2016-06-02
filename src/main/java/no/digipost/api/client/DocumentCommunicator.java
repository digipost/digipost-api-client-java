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

import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.representations.DocumentStatus;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MessageDelivery;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.joda.time.DateTime;

import javax.ws.rs.core.Response;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;

public class DocumentCommunicator extends Communicator {

	public DocumentCommunicator(final ApiService apiService, final EventLogger eventLogger) {
		super(apiService, eventLogger);
	}

	public DocumentEvents getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to, final int offset, final int maxResults) {
		CloseableHttpResponse response = apiService.getDocumentEvents(organisation, partId, from, to, offset, maxResults);
		checkResponse(response, eventLogger);

		try {
			DocumentEvents documentEvents = JAXB.unmarshal(response.getEntity().getContent(), DocumentEvents.class);
			response.close();
			return documentEvents;

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public InputStream getContent(String path) {
		Response response = apiService.getContent(path);
		checkResponse(response, eventLogger);
		return response.readEntity(InputStream.class);
	}

	public DocumentStatus getDocumentStatus(Link linkToDocumentStatus) {
		CloseableHttpResponse response = apiService.getDocumentStatus(linkToDocumentStatus);
		checkResponse(response, eventLogger);

		try {
			DocumentStatus documentStatus = JAXB.unmarshal(response.getEntity().getContent(), DocumentStatus.class);
			response.close();
			return documentStatus;

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public DocumentStatus getDocumentStatus(long senderId, String uuid) {
		CloseableHttpResponse response = apiService.getDocumentStatus(senderId, uuid);
		checkResponse(response, eventLogger);

		try {
			DocumentStatus documentStatus = JAXB.unmarshal(response.getEntity().getContent(), DocumentStatus.class);
			response.close();
			return documentStatus;

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
