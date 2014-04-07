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

import static no.digipost.api.client.Headers.X_Digipost_UserId;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V5;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import no.digipost.api.client.representations.*;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientResponse;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Denne klassen tar seg av de enkelte HTTP-forespørslene man kan gjøre mot
 * REST-API-et, nemlig:
 * 
 * <ul>
 * <li>Hente søkeforslag (autocomplete)</li>
 * <li>Søke etter mottakere</li>
 * <li>Opprette en forsendelsesressurs på serveren
 * <li>Hente en allerede opprettet forsendelsesressurs fra serveren
 * <li>Sende innholdet for en allerede opprettet forsendelsesressurs til
 * serveren, og dermed sende brevet til mottakeren
 * <li>Opprette en printforsendelsesressurs på serveren
 * <li>Hente en allerede opprettet printforsendelsesressurs fra serveren
 * <li>Sende innholdet (PDF) for en allerede opprettet printforsendelsesressurs
 * til serveren, og dermed bestille print av brevet.
 * 
 * <ul>
 * 
 * For å sende et brev gjennom Digipost er det tilstrekkelig å gjøre disse to
 * kallene:
 * 
 * <pre>
 * createMessage(message);
 * addToContentAndSend(createdMessage, content);
 * </pre>
 * 
 * Dette kan også gjøres ved å kalle metoden {@code sendMessage} i klassen
 * {@code MessageSender}, som i tillegg gjør en del feilhåndtering.
 */
public class ApiService {

	private static final String ENTRY_POINT = "/";
	private final WebTarget webResource;
	private final long senderAccountId;

	private EntryPoint cachedEntryPoint;
	private long entryPointLastCached;

	public ApiService(final WebTarget webResource, final long senderAccountId) {
		this.webResource = webResource;
		this.senderAccountId = senderAccountId;
	}

	public EntryPoint getEntryPoint() {
		if (cachedEntryPoint == null || entryPointCacheExpired()) {
			Response response = getEntryPointFromServer();
			if (response.getStatus() != Response.Status.OK.getStatusCode()) {
				throw new DigipostClientException(ErrorType.GENERAL_ERROR, response.readEntity(ErrorMessage.class).getErrorMessage());
			} else {
				cachedEntryPoint = response.readEntity(EntryPoint.class);
				entryPointLastCached = System.currentTimeMillis();
			}
		}
		return cachedEntryPoint;
	}

	private Response getEntryPointFromServer() {
		return webResource.path(ENTRY_POINT)
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	/**
	 * Oppretter en ny forsendelsesressurs på serveren ved å sende en
	 * POST-forespørsel.
	 */
	public Response createMessage(final Message message) {
		EntryPoint entryPoint = getEntryPoint();
		return webResource
				.path(entryPoint.getCreateMessageUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(message, DIGIPOST_MEDIA_TYPE_V5));
	}

	/**
	 * Henter en allerede eksisterende forsendelsesressurs fra serveren.
	 */
	public Response fetchExistingMessage(final URI location) {
		return webResource
				.path(location.getPath())
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	public Response getEncryptionKey(final URI location) {
		return webResource
				.path(location.getPath())
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.get();
	}

	/**
	 * Angir innholdet i en allerede opprettet forsendelse
	 * 
	 * Før man kaller denne metoden, må man allerede ha opprettet en
	 * forsendelsesressurs på serveren ved metoden {@code opprettForsendelse}.
	 * 
	 */
	public Response addContent(final Document document, final InputStream letterContent) {
		Link addContentLink = fetchAddContentLink(document);

		byte[] content = readLetterContent(letterContent);

		return webResource
				.path(addContentLink.getUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(content, MediaType.APPLICATION_OCTET_STREAM_TYPE));
	}

	/**
	 * Sender innholdet i forsendelsen som en POST-forespørsel til serveren
	 * 
	 * OBS! Denne metoden fører til at brevet blir sendt på ordentlig.
	 * 
	 * Før man kaller denne metoden, må man ha lagt innhold til forsendelsen ved
	 * metoden {@code addContent}
	 * 
	 */
	public Response send(final MessageDelivery createdMessage) {
		Link sendLink = fetchSendLink(createdMessage);

		return webResource
				.path(sendLink.getUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.post(null);
	}

	private Link fetchAddContentLink(final Document document) {
		Link addContentLink = document.getAddContentLink();
		if (addContentLink == null) {
			throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold til et dokument som ikke har en link for å gjøre dette.");
		}
		return addContentLink;
	}

	private Link fetchSendLink(final MessageDelivery delivery) {
		Link sendLink = delivery.getSendLink();
		if (sendLink == null) {
			throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST,
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

	public Recipients search(final String searchString) {
		return webResource
				.path(getEntryPoint().getSearchUri().getPath() + "/" + searchString)
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Recipients.class);
	}

	public Autocomplete searchSuggest(final String searchString) {
		return webResource
				.path(getEntryPoint().getAutocompleteUri().getPath() + "/" + searchString)
				.request(MediaTypes.DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Autocomplete.class);
	}

	public void addFilter(final ClientRequestFilter filter) {
		webResource.register(filter);
	}

	private boolean entryPointCacheExpired() {
		int fiveMinutes = 300000;
		return (System.currentTimeMillis() - entryPointLastCached) > fiveMinutes;
	}

	public IdentificationResult identifyRecipient(final Identification identification) {
		return webResource.path(getEntryPoint().getIdentificationUri().getPath())
				.request(DIGIPOST_MEDIA_TYPE_V5)
				.header(X_Digipost_UserId, senderAccountId)
				.post(Entity.entity(identification, DIGIPOST_MEDIA_TYPE_V5), IdentificationResult.class);
	}
}
