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

import no.digipost.api.client.cache.Cache;
import no.digipost.api.client.cache.SingleCached;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderInformation;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.joda.time.DateTime;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static no.digipost.api.client.Headers.X_Digipost_UserId;
import static no.digipost.api.client.cache.CacheConfig.expireAfterAccess;
import static no.digipost.api.client.cache.CacheConfig.useSoftValues;
import static no.digipost.api.client.errorhandling.ErrorCode.PROBLEM_WITH_REQUEST;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V6;
import static no.motif.Singular.optional;
import static no.motif.Strings.prepend;
import static org.joda.time.Duration.standardMinutes;

public class ApiServiceImpl implements ApiService {

	private static final String ENTRY_POINT = "/";
	private final WebTarget webResource;
	private final long senderAccountId;


	private final Callable<EntryPoint> entryPoint = new Callable<EntryPoint>() {
		@Override
        public EntryPoint call() throws Exception {
			Response response = webResource.path(ENTRY_POINT)
					.request(DIGIPOST_MEDIA_TYPE_V6)
					.header(X_Digipost_UserId, senderAccountId)
					.get();
			if (response.getStatus() == OK.getStatusCode()) {
				return response.readEntity(EntryPoint.class);
			} else {
				throw new DigipostClientException(response.readEntity(ErrorMessage.class));
			}
        }
	};

	private final SingleCached<EntryPoint> cachedEntryPoint = new SingleCached<>("digipost-entrypoint", entryPoint, expireAfterAccess(standardMinutes(5)), useSoftValues);
	private final Cache<String, SenderInformation> senderInformation = new Cache<>("sender-information", expireAfterAccess(standardMinutes(5)), useSoftValues);
	private final EventLogger eventLogger;

	public ApiServiceImpl(WebTarget webResource, long senderAccountId, EventLogger eventLogger) {
		this.webResource = webResource;
		this.senderAccountId = senderAccountId;
		this.eventLogger = eventLogger;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return cachedEntryPoint.get();
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
	public Response identifyAndGetEncryptionKey(final Identification identification) {
		EntryPoint entryPoint = getEntryPoint();
		return webResource
				.path(entryPoint.getIdentificationWithEncryptionKeyUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(identification, DIGIPOST_MEDIA_TYPE_V6));
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
	public Response getEncryptionKeyForPrint() {
		EntryPoint entryPoint = getEntryPoint();
		return webResource
				.path(entryPoint.getPrintEncryptionKey().getPath())
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
				.post(Entity.entity(content, APPLICATION_OCTET_STREAM_TYPE));
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
			throw new DigipostClientException(PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold til et dokument som ikke har en link for å gjøre dette.");
		}
		return addContentLink;
	}

	private Link fetchSendLink(final MessageDelivery delivery) {
		Link sendLink = delivery.getSendLink();
		if (sendLink == null) {
			throw new DigipostClientException(PROBLEM_WITH_REQUEST,
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
	public Response getDocumentStatus(Link linkToDocumentStatus) {
		return getDocumentStatus(linkToDocumentStatus.getUri().getPath());
	}

	@Override
	public Response getDocumentStatus(long senderId, String uuid) {
		return getDocumentStatus(String.format("/documents/%s/%s/status", senderId, uuid));
	}

	private Response getDocumentStatus(String path) {
		return webResource
				.path(path)
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
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Autocomplete.class);
	}

	@Override
	public void addFilter(final ClientRequestFilter filter) {
		webResource.register(filter);
	}

	@Override
	public Response identifyRecipient(final Identification identification) {
		return webResource.path(getEntryPoint().getIdentificationUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V6)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(identification, DIGIPOST_MEDIA_TYPE_V6));
	}

	@Override
    public SenderInformation getSenderInformation(long senderId) {
		return senderInformation.get(String.valueOf(senderId),
				getResource(getEntryPoint().getSenderInformationUri().getPath() + "/" + senderId, SenderInformation.class));
    }

	@Override
    public SenderInformation getSenderInformation(String orgnr, String avsenderenhet) {
		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
		queryParams.putSingle("org_id", orgnr);
		if (avsenderenhet != null) {
			queryParams.putSingle("part_id", avsenderenhet);
		}

		return senderInformation.get(orgnr + optional(avsenderenhet).map(prepend("-")).orElse(""),
				getResource(getEntryPoint().getSenderInformationUri().getPath(), queryParams, SenderInformation.class));
    }



	private <R> Callable<R> getResource(final String path, final Class<R> entityType) {
		return getResource(path, new MultivaluedHashMap<String, Object>(), entityType);
	}

	private <R, P> Callable<R> getResource(final String path, final MultivaluedMap<String, P> queryParams, final Class<R> entityType) {
		return new Callable<R>() {
			@Override
            public R call() {
				WebTarget target = webResource.path(path);
				for (Entry<String, List<P>> param : queryParams.entrySet()) {
					target = target.queryParam(param.getKey(), param.getValue().toArray());
				}
				Response response = target.request(DIGIPOST_MEDIA_TYPE_V6).header(X_Digipost_UserId, senderAccountId).get();

				Communicator.checkResponse(response, eventLogger);
				return response.readEntity(entityType);
            }};
	}

}
