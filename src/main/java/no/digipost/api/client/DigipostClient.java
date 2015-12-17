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

import no.digipost.api.client.delivery.ApiFlavor;
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
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.FileKeystoreSigner;
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.util.JerseyClientProvider;
import no.digipost.print.validate.PdfValidator;
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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

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
	private final MessageSender messageSender;
	private final MessageDeliverer deliverer;
	private final DocumentCommunicator documentCommunicator;
	private final DigipostClientConfig config;

	private final ResponseSignatureFilter responseSignatureFilter;
	private final ResponseContentSHA256Filter responseHashFilter = new ResponseContentSHA256Filter();
	private final ResponseDateFilter responseDateFilter = new ResponseDateFilter();


	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final InputStream certificateP12File, final String certificatePassword) {
		this(config, deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), NOOP_EVENT_LOGGER, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer) {
		this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final ApiService apiService) {
		this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, apiService);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final Client jerseyClient) {
		this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, jerseyClient, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger, final ApiService apiService) {
		this(config, deliveryType, digipostUrl, senderAccountId, signer, eventLogger, null, apiService);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final InputStream certificateP12File, final String certificatePassword, final EventLogger eventLogger, final Client jerseyClient) {
		this(config, deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), eventLogger, jerseyClient, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger, final Client jerseyClient, final ApiService overriddenApiService) {

		WebTarget webTarget = (jerseyClient == null ? JerseyClientProvider.newClient() : jerseyClient).register(new GZipEncoder()).target(digipostUrl);

		this.apiService = overriddenApiService == null ? new ApiServiceImpl(webTarget, senderAccountId, eventLogger) : overriddenApiService;
		this.eventLogger = defaultIfNull(eventLogger, NOOP_EVENT_LOGGER);
		this.messageSender = new MessageSender(config, apiService, eventLogger, new PdfValidator());
		this.deliverer = new MessageDeliverer(deliveryType, messageSender);
		this.documentCommunicator = new DocumentCommunicator(apiService, eventLogger);
		this.responseSignatureFilter = new ResponseSignatureFilter(apiService);
		this.config = config;

		webTarget.register(new LoggingFilter());
		webTarget.register(new RequestContentSHA256Filter(eventLogger));
		webTarget.register(new RequestDateFilter(eventLogger));
		webTarget.register(new RequestUserAgentFilter());
		webTarget.register(new RequestSignatureFilter(signer, eventLogger));
		webTarget.register(responseDateFilter);
		webTarget.register(responseHashFilter);
		webTarget.register(responseSignatureFilter);

		log("Initialiserte Jersey-klient mot " + digipostUrl);
	}

	/**
	 * Bestemmer klienten skal kaste exception ved feil under validering av serversignatur, eller
	 * om den heller skal logge med log level warn.
	 *
	 * @param throwOnError true hvis den skal kaste exception, false for warn logging
	 */
	public DigipostClient setThrowOnResponseValidationError(final boolean throwOnError) {
		responseDateFilter.setThrowOnError(throwOnError);
		responseHashFilter.setThrowOnError(throwOnError);
		responseSignatureFilter.setThrowOnError(throwOnError);
		return this;
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
		Response response = apiService.identifyRecipient(identification);
		Communicator.checkResponse(response, eventLogger);
		return response.readEntity(IdentificationResult.class);
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


	/**
	 * Hent informasjon om en gitt avsender. Kan enten be om informasjon om
	 * "deg selv", eller en avsender du har fullmakt til å sende post for.
	 *
	 * @param senderId avsender-IDen til avsenderen du vil ha informasjon om.
	 *
	 * @return informasjon om avsenderen, dersom den finnes, og du har tilgang
	 *         til å informasjon om avsenderen. Det er ikke mulig å skille på
	 *         om avsenderen ikke finnes, eller man ikke har tilgang til
	 *         informasjonen.
	 *
	 * @see SenderInformation
	 */
	public SenderInformation getSenderInformation(long senderId) {
		return apiService.getSenderInformation(senderId);
	}

	/**
	 * Hent informasjon om en gitt avsender. Kan enten be om informasjon om
	 * "deg selv", eller en avsender du har fullmakt til å sende post for.
	 *
	 * @param orgnr organisasjonsnummeret til avsenderen (påkrevet).
	 * @param avsenderenhet underenhet for gitt <code>orgnr</code>, dersom
	 *                      aktuelt, eller <code>null</code>.
	 *
	 *
	 * @return informasjon om avsenderen, dersom den finnes, og du har tilgang
	 *         til å informasjon om avsenderen. Det er ikke mulig å skille på
	 *         om avsenderen ikke finnes, eller man ikke har tilgang til
	 *         informasjonen.
	 *
	 * @see SenderInformation
	 */
	public SenderInformation getSenderInformation(String orgnr, String avsenderenhet) {
		return apiService.getSenderInformation(orgnr, avsenderenhet);
	}

	public DocumentStatus getDocumentStatus(final Link linkToDocumentStatus) {
		return documentCommunicator.getDocumentStatus(linkToDocumentStatus);
	}

	public DocumentStatus getDocumentStatus(long senderId, String uuid) {
		return documentCommunicator.getDocumentStatus(senderId, uuid);
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

	public static class DigipostClientBuilder{
		private ApiFlavor deliveryType = ApiFlavor.ATOMIC_REST;
		private final String digipostURL;
		private final long senderAccountId;
		private final InputStream certificateP12File;
		private final String certificatePassword;
		private final Signer signer;
		private final DigipostClientConfig config;
		private ApiService apiService = null;
		private EventLogger eventLogger = NOOP_EVENT_LOGGER;
		private Client client = null;


		public DigipostClientBuilder(String digipostURL, long senderAccountId, InputStream certificateP12File,
									 String certificatePassword, DigipostClientConfig config){
			this.digipostURL = digipostURL;
			this.senderAccountId = senderAccountId;
			this.certificateP12File = certificateP12File;
			this.certificatePassword = certificatePassword;
			this.signer = null;
			this.config = config;
		}

		public DigipostClientBuilder(String digipostURL, long senderAccountId, Signer signer, DigipostClientConfig config){
			this.digipostURL = digipostURL;
			this.senderAccountId = senderAccountId;
			this.certificateP12File = null;
			this.certificatePassword = null;
			this.signer = signer;
			this.config = config;
		}

		public DigipostClientBuilder deliveryType(ApiFlavor deliveryType){
			this.deliveryType = deliveryType;
			return this;
		}

		public DigipostClientBuilder client(Client client){
			this.client = client;
			return this;
		}

		public DigipostClientBuilder eventLogger(EventLogger eventLogger){
			this.eventLogger = eventLogger;
			return this;
		}

		public DigipostClientBuilder apiService(ApiService apiService){
			this.apiService = apiService;
			return this;
		}

		public DigipostClient build(){
			return signer.equals(null) ? new DigipostClient(config, deliveryType, digipostURL, senderAccountId, certificateP12File, certificatePassword, eventLogger, client)
					: new DigipostClient(config, deliveryType, digipostURL, senderAccountId, signer, eventLogger, client, apiService);
		}

	}
}
