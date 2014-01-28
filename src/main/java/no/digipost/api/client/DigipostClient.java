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

import no.digipost.api.client.filters.request.RequestContentSHA256Filter;
import no.digipost.api.client.filters.request.RequestDateFilter;
import no.digipost.api.client.filters.request.RequestSignatureFilter;
import no.digipost.api.client.filters.request.RequestUserAgentFilter;
import no.digipost.api.client.filters.response.ResponseContentSHA256Filter;
import no.digipost.api.client.filters.response.ResponseDateFilter;
import no.digipost.api.client.filters.response.ResponseSignatureFilter;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.security.FileKeystoreSigner;
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.util.JerseyClientProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

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

	public DigipostClient(final String digipostUrl, final long senderAccountId, final Signer signer, final Client jerseyClient) {
		this(digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, jerseyClient);
	}

	public DigipostClient(final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger) {
		this(digipostUrl, senderAccountId, signer, eventLogger, null);
	}

	public DigipostClient(final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger,
			final Client jerseyClient) {
		this.eventLogger = eventLogger != null ? eventLogger : NOOP_EVENT_LOGGER;

		Client client = jerseyClient == null ? JerseyClientProvider.newClient() : jerseyClient;
		WebResource webResource = client.resource(digipostUrl);

		apiService = new ApiService(webResource, senderAccountId);

		webResource.addFilter(new RequestContentSHA256Filter(eventLogger));
		webResource.addFilter(new RequestSignatureFilter(signer, eventLogger));
		webResource.addFilter(new RequestDateFilter(eventLogger));
		webResource.addFilter(new RequestUserAgentFilter());
		webResource.addFilter(new ResponseDateFilter());
		webResource.addFilter(new ResponseContentSHA256Filter());
		webResource.addFilter(new ResponseSignatureFilter(apiService));

		log("Initialiserte Jersey-klient mot " + digipostUrl);
	}

	/**
	 * Sender et brev gjennom Digipost i et steg. Dersom mottaker ikke er
	 * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
	 * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
	 * til print.)
	 */
	public MessageDelivery createAndSendMessage(final Message message, final InputStream primaryDocumenteContent) {
		return createAndSendMessage(message, primaryDocumenteContent, primaryDocumenteContent);
	}

	/**
	 * Sender et brev gjennom Digipost i et steg med alternativt innhold for
	 * print (må være PDF). Dersom mottaker ikke er digipostbruker og det ligger
	 * printdetaljer på forsendelsen bestiller vi print av brevet til vanlig
	 * postgang. (Krever at avsender har fått tilgang til print.)
	 */
	public MessageDelivery createAndSendMessage(final Message message, final InputStream primaryDocumentContent, final InputStream printContent) {
		return new MessageSender(apiService, eventLogger).createAndSendMessage(message, primaryDocumentContent, printContent);
	}

	/**
	 * Bestiller print av brevet til utsending gjennom vanlig postgang. Krever
	 * at avsender har tilgang til å sende direkte til print. Dersom mottaker
	 * ikke er digipostbruker og det ligger printdetaljer på forsendelsen
	 * bestiller vi print av brevet til vanlig postgang. (Krever at avsender har
	 * fått tilgang til print.)
	 */
	public MessageDelivery createMessageAndDeliverToPrint(final Message printMessage, final InputStream printMessageContent) {
		if (!printMessage.isDirectPrint()) {
			throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
					+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
		}
		return new MessageSender(apiService, eventLogger).createAndSendMessage(printMessage, null, printMessageContent);
	}

	/**
	 * Oppretter et brev med innhold for sending i tre steg.
	 */
	public MessageDelivery createMessage(final Message message) {
		return new MessageSender(apiService, eventLogger).createOrFetchMessage(message);
	}

	/**
	 * Laster opp innhold til et dokument
	 */
	public MessageDelivery addContent(final MessageDelivery message, final Document document, final InputStream documentContent) {
		return new MessageSender(apiService, eventLogger).addContent(message, document, documentContent);
	}

	/**
	 * Laster opp innhold til et dokument med alternativt innhold for print
	 * (må være PDF).
	 */
	public MessageDelivery addContent(final MessageDelivery message, final Document document, final InputStream documentContent,
									  final InputStream printDocumentContent) {
		return new MessageSender(apiService, eventLogger).addContent(message, document, documentContent, printDocumentContent);
	}

	/**
	 * Sender et brev gjennom Digipost. Dersom mottaker ikke er digipostbruker
	 * og det ligger printdetaljer på forsendelsen bestiller vi print av brevet
	 * til vanlig postgang. (Krever at avsender har fått tilgang til print.)
	 */
	public MessageDelivery sendMessage(final MessageDelivery message) {
		return new MessageSender(apiService, eventLogger).sendMessage(message);
	}

	/**
	 * Sender et brev direkte til print. Krever at avsender tilgang til å sende
	 * direkte til print.
	 */
	public MessageDelivery deliverToPrint(final MessageDelivery printMessage) {
		if (printMessage.getDeliveryMethod() != DeliveryMethod.PRINT) {
			throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
					+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
		}
		return new MessageSender(apiService, eventLogger).sendMessage(printMessage);
	}

	public IdentificationResult identifyRecipient(final Identification identification) {
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
