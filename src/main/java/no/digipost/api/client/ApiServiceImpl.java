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
import no.digipost.api.client.representations.*;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static no.digipost.api.client.Headers.X_Digipost_UserId;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V6;

public class ApiServiceImpl implements ApiService {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceImpl.class);

	private static final String ENTRY_POINT = "/";
	private final WebTarget webResource;
	private final long senderAccountId;

	private EntryPoint cachedEntryPoint;
	private long entryPointLastCached;

	public ApiServiceImpl(final WebTarget webResource, final long senderAccountId) {
		this.webResource = webResource;
		this.senderAccountId = senderAccountId;
	}

	@Override
	public EntryPoint getEntryPoint() {
		if (cachedEntryPoint == null || entryPointCacheExpired()) {
			Response response = getEntryPointFromServer();
			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				ErrorMessage error = response.readEntity(ErrorMessage.class);
				throw new DigipostClientException(error);
			} else {
				cachedEntryPoint = response.readEntity(EntryPoint.class);
				entryPointLastCached = System.currentTimeMillis();
			}
		}
		return cachedEntryPoint;
	}

	private Response getEntryPointFromServer() {
		return webResource.path(ENTRY_POINT)
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	@Override
	public Response multipartMessage(final MultiPart multiPart) {
		EntryPoint entryPoint = getEntryPoint();
		return webResource
				.path(entryPoint.getCreateMessageUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(multiPart, "multipart/mixed"));
	}

	@Override
	public Response createMessage(final Message message) {
		EntryPoint entryPoint = getEntryPoint();
		return webResource
				.path(entryPoint.getCreateMessageUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(message, DIGIPOST_MEDIA_TYPE_V6));
	}

	@Override
	public Response fetchExistingMessage(final URI location) {
		return webResource
				.path(location.getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	@Override
	public Response getEncryptionKey(final URI location) {
		return webResource
				.path(location.getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	@Override
	public Response addContent(final Document document, final InputStream letterContent) {
		Link addContentLink = fetchAddContentLink(document);

		byte[] content = readLetterContent(letterContent);

		return webResource
				.path(addContentLink.getUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE));
	}

	@Override
	public Response send(final MessageDelivery createdMessage) {
		Link sendLink = fetchSendLink(createdMessage);

		return webResource
				.path(sendLink.getUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(null);
	}

	private Link fetchAddContentLink(final Document document) {
		Link addContentLink = document.getAddContentLink();
		if (addContentLink == null) {
			throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold til et dokument som ikke har en link for å gjøre dette.");
		}
		return addContentLink;
	}

	private Link fetchSendLink(final MessageDelivery delivery) {
		Link sendLink = delivery.getSendLink();
		if (sendLink == null) {
			throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST,
					"Kan ikke sende en forsendelse som ikke har en link for å gjøre dette.");
		}
		return sendLink;
	}

	byte[] readLetterContent(final InputStream letterContent) {
		try {
			return IOUtils.toByteArray(letterContent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public Response getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to, final int offset, final int maxResults) {
		WebTarget target = webResource
				.path(getEntryPoint().getDocumentEventsUri().getPath())
				.queryParam("from", from.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
				.queryParam("to", to.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
				.queryParam("offset", String.valueOf(offset))
				.queryParam("maxResults", String.valueOf(maxResults));
		if (organisation != null) {
			target = target.queryParam("org", organisation);
		}
		if (partId != null) {
			target = target.queryParam("part", partId);
		}
		return target
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	@Override
	public Response getContent(String path) {
		return webResource
				.path(path)
				.request()
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	@Override
	public Recipients search(final String searchString) {
		return webResource
				.path(getEntryPoint().getSearchUri().getPath() + "/" + searchString)
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Recipients.class);
	}

	@Override
	public Autocomplete searchSuggest(final String searchString) {
		return webResource
				.path(getEntryPoint().getAutocompleteUri().getPath() + "/" + searchString)
				.request(MediaTypes.DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Autocomplete.class);
	}

	@Override
	public void addFilter(final ClientRequestFilter filter) {
		webResource.register(filter);
	}

	private boolean entryPointCacheExpired() {
		int fiveMinutes = 300000;
		return (System.currentTimeMillis() - entryPointLastCached) > fiveMinutes;
	}

	@Override
	public IdentificationResult identifyRecipient(final Identification identification) {
		return webResource.path(getEntryPoint().getIdentificationUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(identification, DIGIPOST_MEDIA_TYPE_V6), IdentificationResult.class);
	}
}
