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

import no.digipost.api.client.filters.ContentMD5Filter;
import no.digipost.api.client.filters.DateFilter;
import no.digipost.api.client.filters.SignatureFilter;
import no.digipost.api.client.filters.UserAgentFilter;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.Recipients;
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
	 * Sender et brev gjennom Digipost. Denne metoden gjør alle HTTP-kallene som
	 * er nødvendige for å sende brevet. Det vil si at den først gjør et kall
	 * for å opprette en message-ressurs på serveren og deretter poster brevets
	 * innhold.
	 */
	public Message sendMessage(final Message message, final InputStream letterContent) {
		return new MessageSender(apiService, eventLogger).sendMessage(message, letterContent);
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
