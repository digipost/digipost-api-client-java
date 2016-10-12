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
package no.digipost.api.client.userdocuments;

import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.cache.inmemory.SingleCached;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static no.digipost.api.client.Headers.X_Digipost_UserId;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_USERS_V1;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V6;
import static no.digipost.cache.inmemory.CacheConfig.expireAfterAccess;
import static no.digipost.cache.inmemory.CacheConfig.useSoftValues;
import static org.apache.http.HttpStatus.SC_OK;
import static org.joda.time.Duration.standardMinutes;

public class ApiService {

	private static final String ROOT = "/";
	private static final String USER_DOCUMENTS_PATH = "/user-documents";
	private static final String USER_AGREEMENTS_PATH = "/user-agreements";

	private final URI serviceEndpoint;
	private final BrokerId brokerId;
	private final CloseableHttpClient httpClient;

	public ApiService(final URI serviceEndpoint, final BrokerId brokerId, final CloseableHttpClient httpClient) {
		this.serviceEndpoint = serviceEndpoint;
		this.brokerId = brokerId;
		this.httpClient = httpClient;
	}

	public IdentificationResult identifyUser(final SenderId senderId, final UserId userId, final String requestTrackingId, final ResponseHandler<IdentificationResult> handler) {
		HttpPost httpPost = prepareHttpPost(getEntryPoint().getIdentificationUri().getPath());
		httpPost.setEntity(marshallJaxbEntity(new Identification(userId)));
		addRequestTrackingHeader(httpPost, requestTrackingId);
		return executeHttpRequest(httpPost, handler);
	}

	public URI createAgreement(final SenderId senderId, final Agreement agreement, final String requestTrackingId, final ResponseHandler<URI> handler) {
		HttpPost httpPost = prepareHttpPost(userAgreementsPath(senderId));
		httpPost.setEntity(marshallJaxbEntity(agreement));
		addRequestTrackingHeader(httpPost, requestTrackingId);
		return executeHttpRequest(httpPost, handler);
	}

	private String userAgreementsPath(final SenderId senderId) {
		return prependSenderIdToPath(senderId, USER_AGREEMENTS_PATH);
	}

	private String userDocumentsPath(final SenderId senderId) {
		return prependSenderIdToPath(senderId, USER_DOCUMENTS_PATH);
	}

	private String prependSenderIdToPath(final SenderId senderId, final String path) {
		return ROOT + senderId.getId() + path;
	}

	public Agreement getAgreement(final URI agreementURI, final String requestTrackingId, final ResponseHandler<Agreement> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(agreementURI.getPath());
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public GetAgreementResult getAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId, final ResponseHandler<GetAgreementResult> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.getPersonalIdentificationNumber())
				.setParameter("agreement-type", agreementType.getType());
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public Agreements getAgreements(final SenderId senderId, final UserId userId, final String requestTrackingId, final ResponseHandler<Agreements> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.getPersonalIdentificationNumber());
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public void deleteAgrement(final URI agreementPath, final String requestTrackingId, final ResponseHandler<Void> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint).setPath(agreementPath.getPath());
		HttpDelete httpDelete = new HttpDelete(buildUri(uriBuilder));
		addRequestTrackingHeader(httpDelete, requestTrackingId);
		executeHttpRequest(httpDelete, handler);
	}

	public void deleteAgrement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId, final ResponseHandler<Void> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.getPersonalIdentificationNumber())
				.setParameter("agreement-type", agreementType.getType());
		HttpDelete httpDelete = new HttpDelete(buildUri(uriBuilder));
		addRequestTrackingHeader(httpDelete, requestTrackingId);
		executeHttpRequest(httpDelete, handler);
	}

	public Documents getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final LocalDate minDueDate, final String requestTrackingId, final ResponseHandler<Documents> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId))
				.setParameter(UserId.QUERY_PARAM_NAME, userId.getPersonalIdentificationNumber())
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		if (status != null) {
			uriBuilder.setParameter(InvoiceStatus.QUERY_PARAM_NAME, status.getStatus());
		}
		if (minDueDate != null) {
			uriBuilder.setParameter("invoice-due-date-from", minDueDate.toString(ISODateTimeFormat.basicDate()));
		}
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public Document getDocument(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId, final ResponseHandler<Document> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId)
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public void updateInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final InvoiceUpdate invoice, final String requestTrackingId, final ResponseHandler<Void> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId + "/invoice")
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		HttpPost httpPost = new HttpPost(buildUri(uriBuilder));
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpPost, requestTrackingId);
		httpPost.setEntity(marshallJaxbEntity(invoice));
		executeHttpRequest(httpPost, handler);
	}

	public DocumentCount getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final LocalDate minDueDate, final String requestTrackingId, final ResponseHandler<DocumentCount> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/count")
				.setParameter(UserId.QUERY_PARAM_NAME, userId.getPersonalIdentificationNumber())
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		if (status != null) {
			uriBuilder.setParameter(InvoiceStatus.QUERY_PARAM_NAME, status.getStatus());
		}
		if (minDueDate != null) {
			uriBuilder.setParameter("invoice-due-date-from", minDueDate.toString(ISODateTimeFormat.basicDate()));
		}
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public DocumentContent getDocumentContent(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId, final ResponseHandler<DocumentContent> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId + "/content")
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public Documents getNewDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final Instant newSince, final Instant notNewerThan, final String requestTrackingId, final ResponseHandler<Documents> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId))
				.setParameter(UserId.QUERY_PARAM_NAME, userId.getPersonalIdentificationNumber())
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		if (status != null) {
			uriBuilder.setParameter(InvoiceStatus.QUERY_PARAM_NAME, status.getStatus());
		}
		if (newSince != null) {
			uriBuilder.setParameter("create-time-min", Long.toString(newSince.getMillis()));
		}
		if (notNewerThan != null) {
			uriBuilder.setParameter("create-time-max", Long.toString(notNewerThan.getMillis()));
		}
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	public AgreementUsers getAgreementUsers(final SenderId senderId, final AgreementType agreementType, final Boolean smsNotificationsEnabled, final String requestTrackingId, final ResponseHandler<AgreementUsers> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId) + "/agreement-users")
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		if (smsNotificationsEnabled != null) {
			uriBuilder
				.setParameter("invoice-sms-notification", smsNotificationsEnabled.toString());
		}

		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return executeHttpRequest(httpGet, handler);
	}

	private <T> T executeHttpRequest(final HttpRequestBase request, final ResponseHandler<T> handler) {
		try {
			request.setHeader(X_Digipost_UserId, brokerId.serialize());
			return httpClient.execute(request, handler);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	private URI buildUri(URIBuilder builder) {
		try {
			return builder.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private HttpPost prepareHttpPost(final String path) {
		HttpPost httpPost = new HttpPost(serviceEndpoint.resolve(path));
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_USERS_V1);
		return httpPost;
	}

	private HttpEntity marshallJaxbEntity(final Object obj) {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXB.marshal(obj, bao);
		return new ByteArrayEntity(bao.toByteArray());
	}

	EntryPoint getEntryPoint() {
		try {
			return cachedEntryPoint.get();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof UserDocumentsApiException) {
				throw (UserDocumentsApiException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	private HttpRequestBase addRequestTrackingHeader(HttpRequestBase request, final String requestTrackingId) {
		if (requestTrackingId != null && requestTrackingId.length() > 0) {
			request.setHeader("X-Digipost-Request-Id", requestTrackingId);
		}
		return request;
	}

	private EntryPoint performGetEntryPoint() {
		HttpGet httpGet = new HttpGet(serviceEndpoint.resolve(ROOT));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		return executeHttpRequest(httpGet, new ResponseHandler<EntryPoint>() {
			@Override
			public EntryPoint handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
				if (response.getStatusLine().getStatusCode() == SC_OK) {
					EntryPoint entryPoint = JAXB.unmarshal(response.getEntity().getContent(), EntryPoint.class);
					return entryPoint;
				} else {
					ErrorMessage errorMessage = JAXB.unmarshal(response.getEntity().getContent(), ErrorMessage.class);
					throw new UnexpectedResponseException(response.getStatusLine(), Error.fromErrorMessage(errorMessage));
				}
			}
		});
	}

	private final Callable<EntryPoint> entryPoint = new Callable<EntryPoint>() {
		@Override
		public EntryPoint call() {
			return performGetEntryPoint();
		}
	};

	private final SingleCached<EntryPoint> cachedEntryPoint = new SingleCached<>("digipost-entrypoint", entryPoint, expireAfterAccess(standardMinutes(5)), useSoftValues);
}
