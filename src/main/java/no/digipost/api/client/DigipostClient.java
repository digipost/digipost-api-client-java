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
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.filters.request.RequestContentSHA256Filter;
import no.digipost.api.client.filters.request.RequestDateInterceptor;
import no.digipost.api.client.filters.request.RequestSignatureInterceptor;
import no.digipost.api.client.filters.request.RequestUserAgentInterceptor;
import no.digipost.api.client.filters.response.ResponseContentSHA256Interceptor;
import no.digipost.api.client.filters.response.ResponseDateInterceptor;
import no.digipost.api.client.filters.response.ResponseSignatureInterceptor;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.representations.DocumentStatus;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.Recipients;
import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.security.FileKeystoreSigner;
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.util.JAXBContextUtils;
import no.digipost.http.client3.DigipostHttpClientFactory;
import no.digipost.http.client3.DigipostHttpClientSettings;
import no.digipost.print.validate.PdfValidator;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;


/**
 * En klient for å sende brev gjennom Digipost. Hvis et objekt av denne klassen
 * er opprettet med et fungerende sertifikat og tilhørende passord, kan man
 * gjøre søk og sende brev gjennom Digipost.
 */
public class DigipostClient {

    static {
        CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();
    }

    public static final EventLogger NOOP_EVENT_LOGGER = eventText -> {};

    private static final Logger LOG = LoggerFactory.getLogger(DigipostClient.class);

	private final EventLogger eventLogger;
	private final ApiService apiService;
	private final MessageSender messageSender;
	private final MessageDeliverer deliverer;
	private final DocumentCommunicator documentCommunicator;
    private final InboxCommunicator inboxCommunicator;

    private final ResponseSignatureInterceptor responseSignatureInterceptor;
    private final ResponseContentSHA256Interceptor responseHashInterceptor = new ResponseContentSHA256Interceptor();
    private final ResponseDateInterceptor responseDateInterceptor = new ResponseDateInterceptor();


    public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl,
                          final long senderAccountId, final InputStream certificateP12File, final String certificatePassword) {
        this(config, deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), NOOP_EVENT_LOGGER, null);
    }

    public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl,
                          final long senderAccountId, final Signer signer, final ApiService apiService) {
        this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, null, apiService, null);
    }

    public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl,
                          final long senderAccountId, final Signer signer, final HttpClientBuilder clientBuilder) {
        this(config, deliveryType, digipostUrl, senderAccountId, signer, NOOP_EVENT_LOGGER, clientBuilder, null, null);
    }

    public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl,
                          final long senderAccountId, final Signer signer, final EventLogger eventLogger,
                          final HttpClientBuilder clientBuilder) {
        this(config, deliveryType, digipostUrl, senderAccountId, signer, eventLogger, clientBuilder, null, null);
    }

    public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl,
                          final long senderAccountId, final InputStream certificateP12File, final String certificatePassword,
                          final EventLogger eventLogger, final HttpClientBuilder clientBuilder, final HttpHost proxy) {
        this(config, deliveryType, digipostUrl, senderAccountId, new FileKeystoreSigner(certificateP12File, certificatePassword), eventLogger, clientBuilder, null, proxy);
    }

    public DigipostClient(final DigipostClientConfig config, final ApiFlavor deliveryType, final String digipostUrl,
                          final long senderAccountId, final Signer signer, final EventLogger eventLogger,
                          final HttpClientBuilder clientBuilder, final ApiService overriddenApiService, final HttpHost proxy) {
        CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();
        HttpClientBuilder httpClientBuilder = clientBuilder == null ? DigipostHttpClientFactory.createBuilder(DigipostHttpClientSettings.DEFAULT) : clientBuilder;
        this.eventLogger = defaultIfNull(eventLogger, NOOP_EVENT_LOGGER);

        this.apiService = overriddenApiService == null ?
                new ApiServiceImpl(httpClientBuilder, senderAccountId, this.eventLogger, digipostUrl, proxy) : overriddenApiService;

		this.messageSender = new MessageSender(config, apiService, this.eventLogger, new PdfValidator());
		this.deliverer = new MessageDeliverer(deliveryType, messageSender);
		this.documentCommunicator = new DocumentCommunicator(apiService, this.eventLogger);
        this.inboxCommunicator = new InboxCommunicator(apiService, this.eventLogger);
        this.responseSignatureInterceptor = new ResponseSignatureInterceptor(apiService);

        apiService.addFilter(new RequestDateInterceptor(this.eventLogger));
        apiService.addFilter(new RequestUserAgentInterceptor());
        apiService.addFilter(new RequestSignatureInterceptor(signer, this.eventLogger, new RequestContentSHA256Filter(this.eventLogger)));
        apiService.addFilter(responseDateInterceptor);
        apiService.addFilter(responseHashInterceptor);
        apiService.addFilter(responseSignatureInterceptor);

        apiService.buildApacheHttpClientBuilder();

        log("Initialiserte apache-klient mot " + digipostUrl);
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
        try(CloseableHttpResponse response = apiService.identifyRecipient(identification))
        {
            Communicator.checkResponse(response, eventLogger);
            return JAXBContextUtils.unmarshal(JAXBContextUtils.identificationContext, response.getEntity().getContent(), IdentificationResult.class);
        } catch (IOException e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e);
        }
    }

    public Recipients search(final String searchString) {
        return apiService.search(searchString);
    }

    public Autocomplete getAutocompleteSuggestions(final String searchString) {
        return apiService.searchSuggest(searchString);
    }

    public DocumentEvents getDocumentEvents(ZonedDateTime from, ZonedDateTime to, int offset, int maxResults) {
        return getDocumentEvents(null, null, from, to, offset, maxResults);
    }

    public DocumentEvents getDocumentEvents(String organisation, String partId, ZonedDateTime from, ZonedDateTime to, int offset, int maxResults) {
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

    public InputStream getContent(final String path) {
        return documentCommunicator.getContent(path);
    }

    /**
     * Get the first 100 documents in the inbox for the organisation represented by senderId.
     *
     * @param senderId Either an organisation that you operate on behalf of or your brokerId
     * @return Inbox element with the 100 first documents
     */
    public Inbox getInbox(SenderId senderId) {
		return getInbox(senderId, 0, 100);
	}

    /**
     * Get documents from the inbox for the organisation represented by senderId.
     *
     * @param senderId Either an organisation that you operate on behalf of or your brokerId
     * @param offset Number of documents to skip. For pagination
     * @param limit Maximum number of documents to retrieve (max 1000)
     * @return Inbox element with the 100 first documents
     */
    public Inbox getInbox(SenderId senderId, int offset, int limit) {
        return inboxCommunicator.getInbox(senderId, offset, limit);
    }

    /**
     * Get the content of a document as a stream. The content is streamed from the server so remember to
     * close the stream to prevent connection leaks.
     *
     * @param inboxDocument The document to get content for
     * @return Entire content of the document as a stream
     */
	public InputStream getInboxDocumentContent(InboxDocument inboxDocument) {
		return inboxCommunicator.getInboxDocumentContentStream(inboxDocument);
	}

    /**
     * Delets the given document from the server
     *
     * @param inboxDocument The document to delete
     */
	public void deleteInboxDocument(InboxDocument inboxDocument) {
		inboxCommunicator.deleteInboxDocument(inboxDocument);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

    public static class DigipostClientBuilder{
        private ApiFlavor deliveryType = ApiFlavor.ATOMIC_REST;
        private final String digipostURL;
        private final long senderAccountId;
        private final InputStream certificateP12File;
        private final String certificatePassword;
        private final Signer signer;
        private final DigipostClientConfig config;
        private HttpClientBuilder clientBuilder = null;
        private EventLogger eventLogger = NOOP_EVENT_LOGGER;
        private ApiService apiService = null;
        private HttpHost proxy = null;

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

        public DigipostClientBuilder proxy(HttpHost proxy){
            this.proxy = proxy;
            return this;
        }

        public DigipostClientBuilder clientBuilder(HttpClientBuilder clientBuilder){
            this.clientBuilder = clientBuilder;
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
            return signer == null ? new DigipostClient(config, deliveryType, digipostURL, senderAccountId, certificateP12File, certificatePassword, eventLogger, clientBuilder, proxy)
                    : new DigipostClient(config, deliveryType, digipostURL, senderAccountId, signer, eventLogger, clientBuilder, apiService, proxy);
        }

    }
}
