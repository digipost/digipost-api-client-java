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
package no.digipost.api.client.internal;

import no.digipost.api.client.BrokerId;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.SenderId;
import no.digipost.api.client.archive.ArchiveApi;
import no.digipost.api.client.delivery.MessageDeliveryApi;
import no.digipost.api.client.document.DocumentApi;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.inbox.InboxApi;
import no.digipost.api.client.internal.http.Headers;
import no.digipost.api.client.internal.http.MultipartNoLengthCheckHttpEntity;
import no.digipost.api.client.internal.http.request.interceptor.RequestContentHashFilter;
import no.digipost.api.client.internal.http.request.interceptor.RequestDateInterceptor;
import no.digipost.api.client.internal.http.request.interceptor.RequestSignatureInterceptor;
import no.digipost.api.client.internal.http.request.interceptor.RequestUserAgentInterceptor;
import no.digipost.api.client.internal.http.response.interceptor.ResponseContentSHA256Interceptor;
import no.digipost.api.client.internal.http.response.interceptor.ResponseDateInterceptor;
import no.digipost.api.client.internal.http.response.interceptor.ResponseSignatureInterceptor;
import no.digipost.api.client.representations.AddDataLink;
import no.digipost.api.client.representations.AdditionalData;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.representations.DocumentStatus;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.Recipients;
import no.digipost.api.client.representations.accounts.UserAccount;
import no.digipost.api.client.representations.accounts.UserInformation;
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;
import no.digipost.api.client.representations.archive.ArchiveDocumentContent;
import no.digipost.api.client.representations.archive.Archives;
import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;
import no.digipost.api.client.representations.sender.AuthorialSender;
import no.digipost.api.client.representations.sender.AuthorialSender.Type;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.Digester;
import no.digipost.api.client.security.Signer;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static javax.xml.bind.JAXB.unmarshal;
import static no.digipost.api.client.internal.ExceptionUtils.asUnchecked;
import static no.digipost.api.client.internal.ExceptionUtils.exceptionNameAndMessage;
import static no.digipost.api.client.internal.http.Headers.Accept_DIGIPOST_MEDIA_TYPE_V7;
import static no.digipost.api.client.internal.http.Headers.Content_Type_DIGIPOST_MEDIA_TYPE_V7;
import static no.digipost.api.client.internal.http.Headers.X_Digipost_UserId;
import static no.digipost.api.client.internal.http.UriUtils.withQueryParams;
import static no.digipost.api.client.internal.http.response.HttpResponseUtils.checkResponse;
import static no.digipost.api.client.internal.http.response.HttpResponseUtils.safelyOfferEntityStreamExternally;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static no.digipost.api.client.util.JAXBContextUtils.unmarshal;

public class ApiServiceImpl implements MessageDeliveryApi, InboxApi, DocumentApi, ArchiveApi {

    private static final Logger LOG = LoggerFactory.getLogger(ApiServiceImpl.class);

    private static final String ENTRY_POINT = "/";
    private final BrokerId brokerId;
    private final CloseableHttpClient httpClient;
    private final URI digipostUrl;

    private final Cached cached;
    private final EventLogger eventLogger;

    // This pattern is hard to get right. The same pattern may not yield the same strings when formatting dates with Joda vs. Java time,
    // which was the case for the pattern "yyyy-MM-dd'T'HH:mm:ss.SSSZZ". See commit messages for 59caeb5737e45a15 and dcf41785a84f42caf935 for details.
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");

    public ApiServiceImpl(DigipostClientConfig config, HttpClientBuilder httpClientBuilder, BrokerId brokerId, Signer signer) {
        this.brokerId = brokerId;
        this.eventLogger = config.eventLogger.withDebugLogTo(LOG);
        this.digipostUrl = config.digipostApiUri;

        this.cached = new Cached(() -> fetchEntryPoint(Optional.empty()));
        this.httpClient = httpClientBuilder
            .addInterceptorLast(new RequestDateInterceptor(config.eventLogger, config.clock))
            .addInterceptorLast(new RequestUserAgentInterceptor())
            .addInterceptorLast(new RequestSignatureInterceptor(signer, config.eventLogger, new RequestContentHashFilter(config.eventLogger, Digester.sha256, Headers.X_Content_SHA256)))
            .addInterceptorLast(new ResponseDateInterceptor(config.clock))
            .addInterceptorLast(new ResponseContentSHA256Interceptor())
            .addInterceptorLast(new ResponseSignatureInterceptor(this::getEntryPoint))
            .build();
        this.eventLogger.log("Initialiserte apache-klient mot " + config.digipostApiUri);
    }

    //Kan sende inn null. Man får da det samme som getEntryPoint()
    public EntryPoint getEntryPoint(SenderId senderId){
        return Optional.ofNullable(senderId)
                .map(specifiedSender -> this.cached.senderEntryPoint.get(specifiedSender, () -> this.fetchEntryPoint(Optional.of(specifiedSender))))
                .orElse(getEntryPoint());
    }
    
    public EntryPoint getEntryPoint() {
        return cached.entryPoint.get();
    }


    @Override
    public CloseableHttpResponse sendMultipartMessage(HttpEntity multipart) {
        MultipartNoLengthCheckHttpEntity multipartLengthCheckHttpEntity = new MultipartNoLengthCheckHttpEntity(multipart);

        EntryPoint entryPoint = getEntryPoint();

        HttpPost httpPost = new HttpPost(digipostUrl.resolve(entryPoint.getCreateMessageUri().getPath()));
        httpPost.setHeader(Accept_DIGIPOST_MEDIA_TYPE_V7);
        httpPost.setHeader("MIME-Version", "1.0");
        httpPost.removeHeaders("Accept-Encoding");
        httpPost.setEntity(multipartLengthCheckHttpEntity);
        return send(httpPost);

    }

    @Override
    public CloseableHttpResponse sendMultipartArchive(HttpEntity multipart) {
        MultipartNoLengthCheckHttpEntity multipartLengthCheckHttpEntity = new MultipartNoLengthCheckHttpEntity(multipart);

        EntryPoint entryPoint = getEntryPoint();

        HttpPost httpPost = new HttpPost(digipostUrl.resolve(entryPoint.getArchiveDocumentsUri().getPath()));
        httpPost.setHeader(Accept_DIGIPOST_MEDIA_TYPE_V7);
        httpPost.setHeader("MIME-Version", "1.0");
        httpPost.removeHeaders("Accept-Encoding");
        httpPost.setEntity(multipartLengthCheckHttpEntity);
        return send(httpPost);

    }

    @Override
    public Archive getArchiveDocument(URI uri) {
        return getEntity(Archive.class, uri.getPath());
    }

    @Override
    public ArchiveDocumentContent getArchiveDocumentContent(URI uri) {
        return getEntity(ArchiveDocumentContent.class, uri.getPath());
    }

    @Override
    public InputStream getArchiveDocumentContentStream(URI uri) {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.WILDCARD.toString());
        final HttpCoreContext httpCoreContext = HttpCoreContext.create();
        httpCoreContext.setAttribute(ResponseSignatureInterceptor.NOT_SIGNED_RESPONSE, true);
        return requestStream(httpGet);
    }

    @Override
    public CloseableHttpResponse identifyAndGetEncryptionKey(Identification identification) {
        EntryPoint entryPoint = getEntryPoint();
        return sendDigipostMedia(identification, entryPoint.getIdentificationWithEncryptionKeyUri().getPath());
    }

    @Override
    public CloseableHttpResponse getEncryptionKey(URI location) {
        HttpGet httpGet = new HttpGet(location);
        httpGet.setHeader(Accept_DIGIPOST_MEDIA_TYPE_V7);
        return send(httpGet);
    }

    @Override
    public CloseableHttpResponse getEncryptionCertificateForPrint() {
        EntryPoint entryPoint = getEntryPoint();

        HttpGet httpGet = new HttpGet(digipostUrl.resolve(entryPoint.getPrintEncryptionCertificate().getPath()));
        httpGet.setHeader(Accept_DIGIPOST_MEDIA_TYPE_V7);
        return send(httpGet);
    }

    @Override
    public CloseableHttpResponse addData(AddDataLink addDataLink, AdditionalData data) {
        return sendDigipostMedia(data, addDataLink.getPath());
    }


    @Override
    public DocumentEvents getDocumentEvents(String organisation, String partId, ZonedDateTime from, ZonedDateTime to, int offset, int maxResults) {
        URIBuilder builder = new URIBuilder(digipostUrl.resolve(getEntryPoint().getDocumentEventsUri().getPath()))
                .setParameter("from", DATE_TIME_FORMAT.format(from))
                .setParameter("to", DATE_TIME_FORMAT.format(to))
                .setParameter("offset", String.valueOf(offset))
                .setParameter("maxResults", String.valueOf(maxResults));

        if (organisation != null) {
            builder = builder.setParameter("org", organisation);
        }
        if (partId != null) {
            builder = builder.setParameter("part", partId);
        }

        try {
            HttpGet httpGet = new HttpGet(builder.build());
            return requestEntity(httpGet, DocumentEvents.class);
        } catch (URISyntaxException e) {
            throw asUnchecked(e);
        }
    }

    @Override
    public DocumentStatus getDocumentStatus(Link linkToDocumentStatus) {
        return getDocumentStatus(linkToDocumentStatus.getUri().getPath());
    }

    @Override
    public DocumentStatus getDocumentStatus(SenderId senderId, UUID uuid) {
        return getDocumentStatus("/documents/" + senderId.stringValue() + "/" + uuid + "/status");
    }

    private DocumentStatus getDocumentStatus(String path) {
        HttpGet httpGet = new HttpGet(digipostUrl.resolve(path));
        return requestEntity(httpGet, DocumentStatus.class);
    }

    @Override
    public InputStream getDocumentContent(String path) {
        HttpGet httpGet = new HttpGet(digipostUrl.resolve(path));
        return requestStream(httpGet);
    }

    @Override
    public Recipients search(String searchString) {
        HttpGet httpGet = new HttpGet(digipostUrl.resolve(createEncodedURIPath(getEntryPoint().getSearchUri().getPath() + "/" + searchString)));
        return requestEntity(httpGet, Recipients.class);
    }

    @Override
    public Autocomplete searchSuggest(String searchString) {
        HttpGet httpGet = new HttpGet(digipostUrl.resolve(createEncodedURIPath(getEntryPoint().getAutocompleteUri().getPath() + "/" + searchString)));
        return requestEntity(httpGet, Autocomplete.class);
    }


    @Override
    public CloseableHttpResponse identifyRecipient(Identification identification) {
        return sendDigipostMedia(identification, getEntryPoint().getIdentificationUri().getPath());
    }

    private EntryPoint fetchEntryPoint(Optional<SenderId> senderId) throws IOException {
        HttpGet httpGet = new HttpGet(digipostUrl.resolve(senderId.map(s -> ENTRY_POINT + s.stringValue()).orElse(ENTRY_POINT)));
        httpGet.setHeader(Accept_DIGIPOST_MEDIA_TYPE_V7);
        final HttpCoreContext httpCoreContext = HttpCoreContext.create();
        httpCoreContext.setAttribute(ResponseSignatureInterceptor.NOT_SIGNED_RESPONSE, true);
        try (CloseableHttpResponse response = send(httpGet, httpCoreContext)) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return unmarshal(jaxbContext, response.getEntity().getContent(), EntryPoint.class);
            } else {
                ErrorMessage errorMessage = unmarshal(jaxbContext, response.getEntity().getContent(), ErrorMessage.class);
                throw new DigipostClientException(errorMessage);
            }
        }
    }



    @Override
    public SenderInformation getSenderInformation(SenderId senderId) {
        return cached.senderInformation.get(senderId.stringValue(),
                () -> getEntity(SenderInformation.class, getEntryPoint().getSenderInformationUri().getPath() + "/" + senderId.stringValue()));
    }

    @Override
    public SenderInformation getSenderInformation(String orgnr, String avsenderenhet) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("org_id", orgnr);
        if (avsenderenhet != null) {
            queryParams.put("part_id", avsenderenhet);
        }

        return cached.senderInformation.get(orgnr + ofNullable(avsenderenhet).map(enhet -> "-" + enhet).orElse(""),
                () -> getEntity(SenderInformation.class, getEntryPoint().getSenderInformationUri().getPath(), queryParams));
    }

    @Override
    public SenderInformation getSenderInformation(MayHaveSender mayHaveSender) {
        AuthorialSender authorialSender = AuthorialSender.resolve(brokerId, mayHaveSender);
        if (authorialSender.is(Type.ACCOUNT_ID)) {
            return getSenderInformation(authorialSender.getAccountId());
        } else {
            return getSenderInformation(authorialSender.getOrganization().organizationId, authorialSender.getOrganization().partId);
        }
    }

    @Override
    public Archives getArchives(SenderId senderId) {
        final URI uri = getEntryPoint(senderId).getArchivesUri();
        return getEntity(Archives.class, uri.getPath());
    }

    @Override
    public Archive getArchiveDocuments(URI uri) {
        return getEntity(Archive.class, pathWithQuery(uri));
    }
    
    @Override
    public Archives getArchiveDocumentsByReferenceId(SenderId senderId, String referenceId) {
        final URI uri = getEntryPoint(senderId).getArchiveDocumentByReferenceUri(referenceId);
        return getEntity(Archives.class, uri.getPath());
    }
    
    @Override
    public Archive getArchiveDocumentByUUID(SenderId senderId, UUID uuid) {
        final URI uri = getEntryPoint(senderId).getArchiveDocumentByUUIDUri(uuid);
        return getEntity(Archive.class, uri.getPath());
    }

    @Override
    public void deleteArchiveDocumentByUUID(ArchiveDocument archiveDocument) {
        final URI uri = archiveDocument.deleteArchiveDocumentUri().get();
        send(new HttpDelete(uri));
    }


    @Override
    public Archive addUniqueUUIDToArchiveDocument(SenderId senderId, UUID uuid, UUID newuuid) {
        final URI uri = getEntryPoint(senderId).getArchiveDocumentByUUIDUri(uuid);
        final Archive archive = getEntity(Archive.class, uri.getPath());

        // Det er alltid en unik referanse
        final ArchiveDocument document = archive.getDocuments().get(0);
        final URI addUniqeUUIDUri = document.getAddUniqueUUID().get();

        final ArchiveDocument nyttDokument = new ArchiveDocument(
                newuuid, document.getFileName(), document.getFileType(), document.getContentType()
        );

        try (CloseableHttpResponse response = sendDigipostMedia(nyttDokument, addUniqeUUIDUri.getPath())) {
            checkResponse(response, eventLogger);
            
            archive.getDocuments().addAll(unmarshal(jaxbContext, response.getEntity().getContent(), Archive.class).getDocuments());
            
            return archive;
        } catch (IOException e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e);
        }
    }

    @Override
    public Inbox getInbox(SenderId senderId, int offset, int limit) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("offset", String.valueOf(offset));
        queryParams.put("limit", String.valueOf(limit));
        
        return getEntity(Inbox.class, getEntryPoint(senderId).getInboxUri().getPath(), queryParams);
    }

    @Override
    public InputStream getInboxDocumentContentStream(InboxDocument inboxDocument) {
        HttpGet httpGet = new HttpGet(inboxDocument.getContentUri());
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.WILDCARD.toString());
        final HttpCoreContext httpCoreContext = HttpCoreContext.create();
        httpCoreContext.setAttribute(ResponseSignatureInterceptor.NOT_SIGNED_RESPONSE, true);
        return requestStream(httpGet);
    }

    @Override
    public void deleteInboxDocument(InboxDocument inboxDocument) {
        send(new HttpDelete(inboxDocument.getDeleteUri()));
    }

    @Override
    public UserAccount createOrActivateUserAccount(SenderId senderId, UserInformation user) {
        HttpPost httpPost = new HttpPost(digipostUrl.resolve("/" + senderId.stringValue() + "/user-accounts"));
        httpPost.setHeader(Content_Type_DIGIPOST_MEDIA_TYPE_V7);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        marshal(jaxbContext, user, bao);
        httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
        return requestEntity(httpPost, UserAccount.class);
    }

    private static String pathWithQuery(URI uri){
        return uri.getPath() + ((uri.getQuery() != null) ? "?" + uri.getQuery() : "");
    }

    private static URI createEncodedURIPath(String path) {
        try {
            return new URI(null, null, path, null);
        } catch (URISyntaxException e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR, "Error encoding search path because of " + exceptionNameAndMessage(e), e);
        }
    }

    private <R> R getEntity(Class<R> entityType, String resourcePath) {
        return requestEntity(new HttpGet(digipostUrl.resolve(resourcePath)), entityType);
    }

    private <R> R getEntity(Class<R> entityType, String resourcePath, Map<String, ?> queryParams) {
        HttpGet httpGet = new HttpGet(withQueryParams(digipostUrl.resolve(resourcePath), queryParams));
        return requestEntity(httpGet, entityType);
    }

    private <R> InputStream requestStream(HttpRequestBase request) {
        return request(request, InputStream.class, new Header[0]);
    }

    private <R> R requestEntity(HttpRequestBase request, Class<R> entityType) {
        return request(request, entityType, Accept_DIGIPOST_MEDIA_TYPE_V7);
    }

    private <R> R request(HttpRequestBase request, Class<R> entityType, Header ... headers) {
        for (Header header : headers) {
            request.setHeader(header);
        }

        if (entityType == InputStream.class) {
            @SuppressWarnings("unchecked")
            R responseStream = (R) safelyOfferEntityStreamExternally(send(request), eventLogger);
            return responseStream;
        } else {
            try (CloseableHttpResponse response = send(request)) {
                checkResponse(response, eventLogger);
                return unmarshal(response.getEntity().getContent(), entityType);
            } catch (IOException e) {
                throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e.getMessage(), e);
            }
        }

    }

    private CloseableHttpResponse send(HttpRequestBase request){
        return send(request, null);
    }

    private CloseableHttpResponse send(HttpRequestBase request, HttpContext context){
        try {
            request.setHeader(X_Digipost_UserId, brokerId.stringValue());
            if (context == null) {
                return httpClient.execute(request);
            } else {
                return httpClient.execute(request, context);
            }
        } catch (IOException e) {
            throw asUnchecked(e);
        }
    }

    private CloseableHttpResponse sendDigipostMedia(Object data, String uri) {
        HttpPost httpPost = new HttpPost(digipostUrl.resolve(uri));
        httpPost.setHeader(Accept_DIGIPOST_MEDIA_TYPE_V7);
        httpPost.setHeader(Content_Type_DIGIPOST_MEDIA_TYPE_V7);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        marshal(jaxbContext, data, bao);
        httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
        return send(httpPost);
    }
}
