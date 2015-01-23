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

import no.digipost.api.client.delivery.DeliveryMethod;
import no.digipost.api.client.delivery.MessageDeliverer;
import no.digipost.api.client.delivery.OngoingDelivery;
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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;


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
	private final MessageDeliverer deliverer;
	private final DocumentCommunicator documentCommunicator;

	private final ResponseSignatureFilter responseSignatureFilter;
	private final ResponseContentSHA256Filter responseHashFilter = new ResponseContentSHA256Filter();
	private final ResponseDateFilter responseDateFilter = new ResponseDateFilter();

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final InputStream certificateP12File, final String certificatePassword) {
		this(deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), NOOP_EVENT_LOGGER, null);
	}

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer) {
		this(deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, null);
	}

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final ApiService apiService) {
		this(deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, apiService);
	}

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final Client jerseyClient) {
		this(deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, jerseyClient, null);
	}

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger, final ApiService apiService) {
		this(deliveryType, digipostUrl, senderAccountId, signer, eventLogger, null, apiService);
	}

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final InputStream certificateP12File, final String certificatePassword, final EventLogger eventLogger, final Client jerseyClient) {
		this(deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), eventLogger, jerseyClient, null);
	}

	public DigipostClient(final DeliveryMethod deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger, final Client jerseyClient, final ApiService overriddenApiService) {
		ClientBuilder.newClient();
		Client client = jerseyClient == null ? JerseyClientProvider.newClient() : jerseyClient;
		client.register(new GZipEncoder());
		WebTarget webTarget = client.target(digipostUrl);
		apiService = overriddenApiService == null ? new ApiServiceImpl(webTarget, senderAccountId) : overriddenApiService;
		this.eventLogger = defaultIfNull(eventLogger, NOOP_EVENT_LOGGER);
		deliverer = new MessageDeliverer(deliveryType, new MessageSender(apiService, eventLogger));
		documentCommunicator = new DocumentCommunicator(apiService, eventLogger);


		webTarget.register(new LoggingFilter());
		webTarget.register(new RequestContentSHA256Filter(eventLogger));
		webTarget.register(new RequestDateFilter(eventLogger));
		webTarget.register(new RequestUserAgentFilter());
		webTarget.register(new RequestSignatureFilter(signer, eventLogger));

		webTarget.register(responseDateFilter);
		webTarget.register(responseHashFilter);
		responseSignatureFilter = new ResponseSignatureFilter(apiService);
		webTarget.register(responseSignatureFilter);

		log("Initialiserte Jersey-klient mot " + digipostUrl);
	}

	/**
	 * Bestemmer klienten skal kaste exception ved feil under validering av serversignatur, eller
	 * om den heller skal logge med log level warn.
	 *
	 * @param throwOnError true hvis den skal kaste exception, false for warn logging
	 */
	public void setThrowOnResponseValidationError(final boolean throwOnError) {
		responseDateFilter.setThrowOnError(throwOnError);
		responseHashFilter.setThrowOnError(throwOnError);
		responseSignatureFilter.setThrowOnError(throwOnError);
	}


	/**
	 * Oppretter en forsendelse for sending gjennom Digipost. Dersom mottaker ikke er
	 * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
	 * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
	 * til print.)
	 */
	public OngoingDelivery.WithPrintFallback createMessage(final Message message) {
		return deliverer.createMessage(message);
	}

	/**
	 * Opprette forsendelse som skal gå direkte til print og videre til utsending
	 * gjennom vanlig postgang. Krever at avsender har tilgang til å sende direkte
	 * til print.
	 */
	public OngoingDelivery.ForPrintOnly createPrintOnlyMessage(final Message printMessage) {
		return deliverer.createPrintOnlyMessage(printMessage);
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

	public DocumentEvents getDocumentEvents(final DateTime from, final DateTime to,
	                                        final int offset, final int maxResults) {
		return getDocumentEvents(null, null, from, to, offset, maxResults);
	}

	public DocumentEvents getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to,
	                                        final int offset, final int maxResults) {
		return documentCommunicator.getDocumentEvents(organisation, partId, from, to, offset, maxResults);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	/**
	 * Dersom vi tester mot et av Digiposts testmiljøer, vil vi ikke bruke
	 * SSL-validering.
	 */
	public static JerseyClient createJerseyClientWithoutSSLValidation() {
		TrustManager[] noopTrustManager = new TrustManager[]{new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			}
		}};

		HostnameVerifier noopHostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(final String hostname, final SSLSession session) {
				return true;
			}
		};

		SSLContext sc;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, noopTrustManager, new SecureRandom());
			ClientConfig c = new ClientConfig();
			c.register(LoggingFilter.class);
			c.register(MultiPartFeature.class);

			return new JerseyClientBuilder().sslContext(sc).withConfig(c).hostnameVerifier(noopHostnameVerifier).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public InputStream getContent(final String path) {
		return documentCommunicator.getContent(path);
	}
}
