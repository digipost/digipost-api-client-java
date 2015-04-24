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
import org.joda.time.DateTime;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class DocumentCommunicator extends Communicator {

	public DocumentCommunicator(final ApiService apiService, final EventLogger eventLogger) {
		super(apiService, eventLogger);
	}

	public DocumentEvents getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to, final int offset, final int maxResults) {
		Response response = apiService.getDocumentEvents(organisation, partId, from, to, offset, maxResults);
		checkResponse(response, eventLogger);
		return response.readEntity(DocumentEvents.class);
	}

	public InputStream getContent(String path) {
		Response response = apiService.getContent(path);
		checkResponse(response, eventLogger);
		return response.readEntity(InputStream.class);
	}

	public DocumentStatus getDocumentStatus(Link linkToDocumentStatus) {
		Response response = apiService.getDocumentStatus(linkToDocumentStatus);
		checkResponse(response, eventLogger);
		return response.readEntity(DocumentStatus.class);
	}

	public DocumentStatus getDocumentStatus(long senderId, String uuid) {
		Response response = apiService.getDocumentStatus(senderId, uuid);
		checkResponse(response, eventLogger);
		return response.readEntity(DocumentStatus.class);
	}
}
