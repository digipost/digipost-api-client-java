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
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

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

	private EventLogger eventLogger = null;
	private ApiService apiService = null;

	public DigipostClient(final String digipostUrl, final long senderAccountId, final InputStream certificateP12File,
						  final String certificatePassword) {
		this(digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), NOOP_EVENT_LOGGER);
	}

	public DigipostClient(final String digipostUrl, final long senderAccountId, final InputStream certificateP12File,
						  final String sertifikatPassord, final EventLogger eventLogger, final Client jerseyClient) {
		this(digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, sertifikatPassord), eventLogger,
				jerseyClient);
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
		WebTarget webTarget = client.target(digipostUrl);

		apiService = new ApiService(webTarget, senderAccountId);

		webTarget.register(new LoggingFilter());
		webTarget.register(new RequestContentSHA256Filter(eventLogger));
		webTarget.register(new RequestDateFilter(eventLogger));
		webTarget.register(new RequestUserAgentFilter());
		webTarget.register(new RequestSignatureFilter(signer, eventLogger));

		webTarget.register(new ResponseDateFilter());
		webTarget.register(new ResponseContentSHA256Filter());
		webTarget.register(new ResponseSignatureFilter(apiService));

		log("Initialiserte Jersey-klient mot " + digipostUrl);
	}

	/**
	 * Sender en forsendelse gjennom Digipost i ett kall. Dersom mottaker ikke er
	 * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
	 * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
	 * til print.)
	 */

	public MessageDelivery sendMultipartMessage(final Message message, Map<FileMetadata, InputStream> files) {
		FormDataMultiPart fdmp = new FormDataMultiPart();
		fdmp.field("message", message, MediaType.valueOf(MediaTypes.DIGIPOST_MEDIA_TYPE_V5));
		Set<Map.Entry<FileMetadata, InputStream>> entries = files.entrySet();

		for (Map.Entry<FileMetadata, InputStream> e : entries) {
			String uuid = e.getKey().fileName;
			MediaType mediaType = e.getKey().mediaType;
			InputStream stream = e.getValue();
			fdmp.field(uuid, stream, mediaType);
		}

		MessageSender sender = new MessageSender(apiService, eventLogger);
		return sender.createMultipartMessage(fdmp);
	}

	/**
	 * Oppretter en forsendelse for sending gjennom Digipost. Dersom mottaker ikke er
	 * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
	 * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
	 * til print.)
	 */
	public OngoingDelivery.WithPrintFallback createMessage(final Message message) {
		return new OngoingDelivery.SendableWithPrintFallback() {

			private final MessageSender sender = new MessageSender(apiService, eventLogger);
			private MessageDelivery delivery = sender.createOrFetchMessage(message);

			/**
			 * Laster opp innhold til et dokument.
			 *
			 * @return videre operasjoner for å fullføre leveransen.
			 */
			@Override
			public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content) {
				return addContent(document, content, content);
			}


			@Override
			public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content, InputStream printContent) {
				delivery = sender.addContent(delivery, delivery.getDocumentByUuid(document.getUuid()), content, printContent);
				return this;
			}

			/**
			 * Sender forsendelsen gjennom Digipost. Dersom mottaker ikke er Digipostbruker
			 * og det ligger printdetaljer på forsendelsen bestiller vi print av brevet
			 * til vanlig postgang. (Krever at avsender har fått tilgang til print.)
			 */
			@Override
			public MessageDelivery send() {
				delivery = sender.sendMessage(delivery);
				return delivery;
			}

		};
	}

	/**
	 * Opprette forsendelse som skal gå direkte til print og videre til utsending
	 * gjennom vanlig postgang. Krever at avsender har tilgang til å sende direkte
	 * til print.
	 */
	public OngoingDelivery.ForPrintOnly createPrintOnlyMessage(final Message printMessage) {
		return new OngoingDelivery.SendableForPrintOnly() {

			private final MessageSender sender;
			private MessageDelivery delivery;

			{
				if (!printMessage.isDirectPrint()) {
					throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
							+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
				}
				sender = new MessageSender(apiService, eventLogger);
				delivery = sender.createOrFetchMessage(printMessage);
			}


			/**
			 * Laster opp innhold til et dokument. Merk: må være PDF-format.
			 *
			 * @return videre operasjoner for å fullføre leveransen.
			 */
			@Override
			public OngoingDelivery.SendableForPrintOnly addContent(Document document, InputStream printContent) {
				delivery = sender.addContent(delivery, delivery.getDocumentByUuid(document.getUuid()), null, printContent);
				return this;
			}

			@Override
			public MessageDelivery send() {
				if (delivery.getDeliveryMethod() != DeliveryMethod.PRINT) {
					throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
							+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
				}
				delivery = sender.sendMessage(delivery);
				return delivery;
			}
		};
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
}
