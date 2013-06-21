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
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V3;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import no.digipost.api.client.representations.Attachment;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.ContentType;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MediaTypes;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.Recipients;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Denne klassen tar seg av de enkelte HTTP-forespørslene man kan gjøre mot
 * REST-API-et, nemlig:
 * 
 * <ul>
 * <li>Hente søkeforslag (autocomplete)</li>
 * <li>Søke etter mottakere</li>
 * <li>Opprette en forsendelsesressurs på serveren
 * <li>Hente en allerede opprettet forsendelsesressurs fra serveren
 * <li>Sende innholdet (PDF) for en allerede opprettet forsendelsesressurs til
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
	private final WebResource webResource;
	private final long senderAccountId;

	private EntryPoint cachedEntryPoint;
	private long entryPointLastCached;

	public ApiService(final WebResource webResource, final long senderAccountId) {
		this.webResource = webResource;
		this.senderAccountId = senderAccountId;
	}

	public EntryPoint getEntryPoint() {
		if (cachedEntryPoint == null || entryPointCacheExpired()) {
			ClientResponse response = getEntryPointFromServer();
			if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
				throw new DigipostClientException(ErrorType.GENERAL_ERROR, response.getEntity(ErrorMessage.class).getErrorMessage());
			} else {
				cachedEntryPoint = response.getEntity(EntryPoint.class);
				entryPointLastCached = System.currentTimeMillis();
			}
		}
		return cachedEntryPoint;
	}

	private ClientResponse getEntryPointFromServer() {
		return webResource
				.path(ENTRY_POINT)
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.get(ClientResponse.class);
	}

	/**
	 * Oppretter en ny forsendelsesressurs på serveren ved å sende en
	 * POST-forespørsel. Brukes for å sende brev i Digipost.
	 */
	public ClientResponse createMessage(final Message message) {
		EntryPoint entryPoint = getEntryPoint();
		return webResource
				.path(entryPoint.getCreateMessageUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(DIGIPOST_MEDIA_TYPE_V3)
				.post(ClientResponse.class, message);
	}

	/**
	 * Oppretter en ny printforsendelsesressurs på serveren ved å sende en
	 * POST-forespørsel. Brukes for bestille print av et brev dersom mottaker
	 * ikke er Digipostbruker.
	 * 
	 */
	public ClientResponse createPrintMessage(final Message message, final URI createPrintMessageUri) {
		return webResource
				.path(createPrintMessageUri.getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(DIGIPOST_MEDIA_TYPE_V3)
				.post(ClientResponse.class, message);
	}

	/**
	 * Oppretter en ny vedleggsressurs på serveren ved å sende en
	 * POST-forespørsel. Brukes for å legge vedlegg til et brev i Digipost.
	 * 
	 */
	public ClientResponse createAttachment(final MessageDelivery message, final Attachment attachment) {
		return webResource
				.path(fetchAddAttachmentLink(message).getUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(DIGIPOST_MEDIA_TYPE_V3)
				.post(ClientResponse.class, attachment);
	}

	/**
	 * Henter en allerede eksisterende forsendelsesressurs fra serveren.
	 */
	public ClientResponse fetchExistingMessage(final URI location) {
		return webResource
				.path(location.getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.get(ClientResponse.class);
	}

	public ClientResponse getEncryptionKey(final URI location) {
		return webResource
				.path(location.getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.get(ClientResponse.class);
	}

	/**
	 * Angir innholdet i en allerede opprettet forsendelse og sender det som en
	 * POST-forespørsel til serveren.
	 * 
	 * OBS! Denne metoden fører til at brevet blir sendt på ordentlig.
	 * 
	 * Før man kaller denne metoden, må man allerede ha opprettet en
	 * forsendelsesressurs på serveren ved metoden {@code opprettForsendelse}.
	 * 
	 */
	public ClientResponse addContentAndSend(final MessageDelivery createdMessage, final InputStream letterContent,
			final ContentType contentType) {
		Link addContentAndSendLink = fetchAddContentAndSendLink(createdMessage);

		byte[] content = readLetterContent(letterContent);

		return webResource
				.path(addContentAndSendLink.getUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(contentType.getRequestMediaType())
				.post(ClientResponse.class, content);

	}

	/**
	 * Angir innholdet i en allerede opprettet forsendelse
	 * 
	 * Før man kaller denne metoden, må man allerede ha opprettet en
	 * forsendelsesressurs på serveren ved metoden {@code opprettForsendelse}.
	 * 
	 */
	public ClientResponse addContent(final MessageDelivery createdMessage, final InputStream letterContent, final ContentType contentType) {
		Link addContentLink = fetchAddContentLink(createdMessage);

		byte[] content = readLetterContent(letterContent);

		return webResource
				.path(addContentLink.getUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(contentType.getRequestMediaType())
				.post(ClientResponse.class, content);
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
	public ClientResponse send(final MessageDelivery createdMessage) {
		Link sendLink = fetchSendLink(createdMessage);

		return webResource
				.path(sendLink.getUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(DIGIPOST_MEDIA_TYPE_V3)
				.post(ClientResponse.class);
	}

	/**
	 * Angir innholdet i et allerede opprettet vedlegg
	 * 
	 * Før man kaller denne metoden, må man allerede ha opprettet en
	 * vedleggsressurs på serveren ved metoden {@code createAttachment}.
	 * 
	 */
	public ClientResponse addContentToAttachment(final Attachment attachment, final InputStream attachmentContent,
			final ContentType contentType) {
		Link addContentLink = fetchAddContentLink(attachment);

		byte[] content = readLetterContent(attachmentContent);

		return webResource
				.path(addContentLink.getUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(contentType.getRequestMediaType())
				.post(ClientResponse.class, content);
	}

	private Link fetchAddAttachmentLink(final MessageDelivery delivery) {
		Link addAttachmentLink = delivery.getAddAttachmentLink();
		if (addAttachmentLink == null) {
			throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST,
					"Kan ikke legge til vedlegg til en forsendelse som ikke har en link til å gjøre dette.");
		}
		return addAttachmentLink;
	}

	private Link fetchAddContentAndSendLink(final MessageDelivery delivery) {
		Link addContentLink = delivery.getAddContentAndSendLink();
		if (addContentLink == null) {
			throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold og sende en forsendelse som ikke har en link for å gjøre dette.");
		}
		return addContentLink;
	}

	private Link fetchAddContentLink(final MessageDelivery delivery) {
		Link addContentLink = delivery.getAddContentLink();
		if (addContentLink == null) {
			throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold til en forsendelse som ikke har en link for å gjøre dette.");
		}
		return addContentLink;
	}

	private Link fetchAddContentLink(final Attachment attachment) {
		Link addContentLink = attachment.getAddContentLink();
		if (addContentLink == null) {
			throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold til et vedlegg som ikke har en link for å gjøre dette.");
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
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Recipients.class);
	}

	public Autocomplete searchSuggest(final String searchString) {
		return webResource
				.path(getEntryPoint().getAutocompleteUri().getPath() + "/" + searchString)
				.accept(MediaTypes.DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.get(Autocomplete.class);
	}

	public void addFilter(final ClientFilter filter) {
		webResource.addFilter(filter);
	}

	private boolean entryPointCacheExpired() {
		int fiveMinutes = 300000;
		return (System.currentTimeMillis() - entryPointLastCached) > fiveMinutes;
	}

	public IdentificationResult identifyRecipient(final Identification identification) {
		return webResource.path(getEntryPoint().getIdentificationUri().getPath())
				.accept(DIGIPOST_MEDIA_TYPE_V3)
				.header(X_Digipost_UserId, senderAccountId)
				.type(DIGIPOST_MEDIA_TYPE_V3)
				.post(IdentificationResult.class, identification);
	}
}
