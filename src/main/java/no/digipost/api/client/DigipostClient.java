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
import no.digipost.api.client.filters.request.*;
import no.digipost.api.client.filters.response.*;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.FileKeystoreSigner;
import no.digipost.api.client.security.Signer;
import no.digipost.print.validate.PdfValidator;
import no.posten.dpost.httpclient.DigipostHttpClientFactory;
import no.posten.dpost.httpclient.DigipostHttpClientSettings;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.xml.bind.JAXB;

import java.io.IOException;
import java.io.InputStream;

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

	private final ResponseSignatureInterceptor responseSignatureInterceptor;
	private final ResponseContentSHA256Interceptor responseHashInterceptor = new ResponseContentSHA256Interceptor();
	private final ResponseDateInterceptor responseDateInterceptor = new ResponseDateInterceptor();


	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final InputStream certificateP12File, final String certificatePassword) {
		this(config, deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), NOOP_EVENT_LOGGER, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final ApiService apiService) {
		this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, apiService);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer) {
		this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final InputStream certificateP12File, final String certificatePassword, final EventLogger eventLogger) {
		this(config, deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), eventLogger, null);
	}

	public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl, final long senderAccountId, final Signer signer, final EventLogger eventLogger, final ApiService overriddenApiService) {
		this.apiService = overriddenApiService == null ? new ApiServiceImpl(senderAccountId, eventLogger, digipostUrl) : overriddenApiService;

		this.eventLogger = defaultIfNull(eventLogger, NOOP_EVENT_LOGGER);
		this.messageSender = new MessageSender(config, apiService, eventLogger, new PdfValidator());
		this.deliverer = new MessageDeliverer(deliveryType, messageSender);
		this.documentCommunicator = new DocumentCommunicator(apiService, eventLogger);
		this.responseSignatureInterceptor = new ResponseSignatureInterceptor(apiService);

		CloseableHttpClient apacheClient = DigipostHttpClientFactory.createBuilder(DigipostHttpClientSettings.DEFAULT)
				.addInterceptorLast(new RequestDateInterceptor(eventLogger))
				.addInterceptorLast(new RequestUserAgentInterceptor())
				.addInterceptorLast(new RequestSignatureInterceptor(signer, eventLogger, new RequestContentSHA256Filter(eventLogger)))
				.addInterceptorLast(responseDateInterceptor)
				.addInterceptorLast(responseHashInterceptor)
				.addInterceptorLast(responseSignatureInterceptor).build();

		apiService.setApacheClient(apacheClient);

		log("Initialiserte Jersey-klient mot " + digipostUrl);
	}

	/**
	 * Bestemmer klienten skal kaste exception ved feil under validering av serversignatur, eller
	 * om den heller skal logge med log level warn.
	 *
	 * @param throwOnError true hvis den skal kaste exception, false for warn logging
	 */
	public DigipostClient setThrowOnResponseValidationError(final boolean throwOnError) {
		responseDateInterceptor.setThrowOnError(throwOnError);
		responseHashInterceptor.setThrowOnError(throwOnError);
		responseSignatureInterceptor.setThrowOnError(throwOnError);
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
		CloseableHttpResponse response = apiService.identifyRecipient(identification);
		Communicator.checkResponse(response, eventLogger);

		try {
			IdentificationResult identificationResult = JAXB.unmarshal(response.getEntity().getContent(), IdentificationResult.class);
			response.close();
			return identificationResult;

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
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
			return signer.equals(null) ? new DigipostClient(config, deliveryType, digipostURL, senderAccountId, certificateP12File, certificatePassword, eventLogger)
					: new DigipostClient(config, deliveryType, digipostURL, senderAccountId, signer, eventLogger, apiService);
		}

	}
}
