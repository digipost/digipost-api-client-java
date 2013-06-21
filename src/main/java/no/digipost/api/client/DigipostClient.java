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

import java.io.InputStream;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import no.digipost.api.client.filters.ContentMD5Filter;
import no.digipost.api.client.filters.DateFilter;
import no.digipost.api.client.filters.SignatureFilter;
import no.digipost.api.client.filters.UserAgentFilter;
import no.digipost.api.client.representations.Attachment;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.ContentType;
import no.digipost.api.client.representations.DeliveryMethod;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.Recipients;
import no.digipost.api.client.security.FileKeystoreSigner;
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.util.JerseyClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * En klient for å sende brev gjennom Digipost. Hvis et objekt av denne klassen
 * er opprettet med et fungerende sertifikat og tilhørende passord, kan man
 * gjøre søk og sende brev gjennom Digipost.
 */
public class DigipostClient {
	public static final EventLogger NOOP_EVENT_LOGGER = new EventLogger() {
		@Override
		public void log(final String eventText) {
			// NOOP
		}
	};

	private static final Logger LOG = LoggerFactory.getLogger(DigipostClient.class);

	private final EventLogger eventLogger;
	private final ApiService apiService;

	public DigipostClient(final String digipostUrl, final long senderAccountId, final InputStream certificateP12File,
			final String certificatePassword) {
		this(digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), NOOP_EVENT_LOGGER);
	}

	public DigipostClient(final String digipostUrl, final long senderAccountId, final InputStream certificateP12File,
			final String sertifikatPassord, final EventLogger eventLogger) {
		this(digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, sertifikatPassord), eventLogger);
	}

	public DigipostClient(final String digipostUrl, final long senderAccountId, final Signer signer) {
		this(digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER);
	}

	public DigipostClient(final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger) {
		this.eventLogger = eventLogger != null ? eventLogger : NOOP_EVENT_LOGGER;

		Client client = JerseyClientProvider.getClient();
		WebResource webResource = client.resource(digipostUrl);
		webResource.addFilter(new ContentMD5Filter(eventLogger));
		webResource.addFilter(new SignatureFilter(signer, eventLogger));
		webResource.addFilter(new DateFilter(eventLogger));
		webResource.addFilter(new UserAgentFilter());
		log("Initialiserte Jersey-klient mot " + digipostUrl);

		apiService = new ApiService(webResource, senderAccountId);
	}

	/**
	 * Sender et brev gjennom Digipost i et steg.
	 * Filtype må være PDF.
	 */
	public MessageDelivery createAndSendMessage(final Message message, final InputStream letterContent) {
		return createAndSendMessage(message, ContentType.PDF, letterContent);
	}

	/**
	 * Sender et brev gjennom Digipost i et steg.
	 * Må definere filtype. Støttede filtyper er PDF og HTML.
	 * Dersom mottaker ikke er digipostbruker og det
	 * ligger printdetaljer på forsendelsen bestiller vi print av brevet til vanlig postgang.
	 * (Krever at avsender har fått tilgang til print.)
	 */
	public MessageDelivery createAndSendMessage(final Message message, final ContentType contentType,
			final InputStream letterContent) {
		return createAndSendMessage(message, contentType, letterContent, letterContent);
	}

	/**
	 * Sender et brev gjennom Digipost i et steg med alternativt innhold for print (må være PDF).
	 * Må definere filtype for innhold til Digipost. Støttede filtyper er PDF og HTML.
	 * Dersom mottaker ikke er digipostbruker og det
	 * ligger printdetaljer på forsendelsen bestiller vi print av brevet til vanlig postgang.
	 * (Krever at avsender har fått tilgang til print.)
	 */
	public MessageDelivery createAndSendMessage(final Message message, final ContentType contentType,
			final InputStream letterContent, final InputStream printContent) {
		return new MessageSender(apiService, eventLogger).createAndSendMessage(message, letterContent,
				contentType, printContent);
	}

	/**
	 * Bestiller print av brevet til utsending gjennom vanlig postgang.
	 * Krever at avsender har tilgang til å sende direkte til print.
	 * Dersom mottaker ikke er digipostbruker og det
	 * ligger printdetaljer på forsendelsen bestiller vi print av brevet til vanlig postgang.
	 * (Krever at avsender har fått tilgang til print.)
	 */
	public MessageDelivery createMessageAndDeliverToPrint(final Message printMessage, final InputStream printMessageContent) {
		if (!printMessage.isDirectPrint()) {
			throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
					+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
		}
		return new MessageSender(apiService, eventLogger).createAndSendMessage(printMessage, null,
				null, printMessageContent);
	}


	/**
	 * Oppretter et brev med innhold for sending i to steg.
	 * Filtype må være PDF.
	 */
	public MessageDelivery createMessage(final Message message, final InputStream letterContent) {
		return createMessage(message, ContentType.PDF, letterContent, letterContent);
	}

	/**
	 * Oppretter et brev med innhold for sending i to steg.
	 * Må definere filtype. Støttede filtyper er PDF og HTML.
	 */
	public MessageDelivery createMessage(final Message message, final ContentType contentType, final InputStream letterContent) {
		return createMessage(message, contentType, letterContent, letterContent);
	}

	/**
	 * Oppretter et brev med innhold for sending i to steg med alternativt innhold for print (må være PDF).
	 * Må definere filtype for innhold til Digipost. Støttede filtyper er PDF og HTML.
	 */
	public MessageDelivery createMessage(final Message message, final ContentType contentType, final InputStream letterContent,
			final InputStream printContent) {
		return new MessageSender(apiService, eventLogger)
		.createMessageAndAddContent(message, letterContent, contentType, printContent);
	}

	/**
	 * Oppretter et vedlegg i Digipost.
	 * Filtype må være PDF.
	 */
	public MessageDelivery createAttachment(final MessageDelivery delivery, final Attachment attachment,
			final InputStream digipostContent) {
		return createAttachment(delivery, attachment, ContentType.PDF, digipostContent, digipostContent);
	}

	/**
	 * Oppretter et vedlegg i Digipost.
	 * Må definere filtype. Støttede filtyper er PDF og HTML.
	 */
	public MessageDelivery createAttachment(final MessageDelivery delivery, final Attachment attachment,
			final ContentType contentType, final InputStream digipostContent) {
		return createAttachment(delivery, attachment, contentType, digipostContent, digipostContent);
	}

	/**
	 * Oppretter et vedlegg i Digipost med alternativt innhold for print (må være PDF).
	 * Må definere filtype for innhold til Digipost. Støttede filtyper er PDF og HTML.
	 */
	public MessageDelivery createAttachment(final MessageDelivery delivery, final Attachment attachment,
			final ContentType contentType, final InputStream digipostContent, final InputStream printContent) {
		return new MessageSender(apiService, eventLogger).createAttachmentAndAddContent(delivery, attachment, digipostContent,
				contentType, printContent);
	}

	/**
	 * Sender et brev gjennom Digipost. Dersom mottaker ikke er digipostbruker og det
	 * ligger printdetaljer på forsendelsen bestiller vi print av brevet til vanlig postgang.
	 * (Krever at avsender har fått tilgang til print.)
	 */
	public MessageDelivery sendMessage(final MessageDelivery message) {
		return new MessageSender(apiService, eventLogger).sendMessage(message);
	}

	/**
	 * Sender et brev direkte til print.
	 * Krever at avsender tilgang til å sende direkte til print.
	 */
	public MessageDelivery deliverToPrint(final MessageDelivery printMessage) {
		if (printMessage.getDeliveryMethod() != DeliveryMethod.PRINT) {
			throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
					+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
		}
		return new MessageSender(apiService, eventLogger).sendMessage(printMessage);
	}

	public IdentificationResult identifyRecipient(Identification identification) {
		return apiService.identifyRecipient(identification);
	}


	public Recipients search(final String searchString) {
		return apiService.search(searchString);
	}

	public Autocomplete getAutocompleteSuggestions(final String searchString) {
		return apiService.searchSuggest(searchString);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	public void addFilter(final ClientFilter filter) {
		apiService.addFilter(filter);
	}
}
