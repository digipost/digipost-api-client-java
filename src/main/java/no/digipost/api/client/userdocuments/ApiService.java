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

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.cache.inmemory.SingleCached;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;

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

	public CloseableHttpResponse identifyUser(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		HttpPost httpPost = prepareHttpPost(getEntryPoint().getIdentificationUri().getPath());
		httpPost.setEntity(marshallJaxbEntity(new Identification(userId)));
		addRequestTrackingHeader(httpPost, requestTrackingId);
		return send(httpPost);
	}

	public CloseableHttpResponse createAgreement(final SenderId senderId, final Agreement agreement, final String requestTrackingId) {
		HttpPost httpPost = prepareHttpPost(userAgreementsPath(senderId));
		httpPost.setEntity(marshallJaxbEntity(agreement));
		addRequestTrackingHeader(httpPost, requestTrackingId);
		return send(httpPost);
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

	public CloseableHttpResponse getAgreement(final URI agreementURI, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(agreementURI.getPath());
		return doGetRequest(requestTrackingId, uriBuilder);
	}

	public CloseableHttpResponse getAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.getPersonalIdentificationNumber())
				.setParameter("agreement-type", agreementType.getType());
		return doGetRequest(requestTrackingId, uriBuilder);
	}

	public CloseableHttpResponse getAgreements(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.getPersonalIdentificationNumber());
		return doGetRequest(requestTrackingId, uriBuilder);
	}

	public CloseableHttpResponse deleteAgrement(final URI agreementPath, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(agreementPath.getPath());
		HttpDelete httpDelete = new HttpDelete(buildUri(uriBuilder));
		addRequestTrackingHeader(httpDelete, requestTrackingId);
		return send(httpDelete);
	}

	public CloseableHttpResponse deleteAgrement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.getPersonalIdentificationNumber())
				.setParameter("agreement-type", agreementType.getType());
		HttpDelete httpDelete = new HttpDelete(buildUri(uriBuilder));
		addRequestTrackingHeader(httpDelete, requestTrackingId);
		return send(httpDelete);
	}

	public CloseableHttpResponse getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId))
				.setParameter(UserId.QUERY_PARAM_NAME, userId.getPersonalIdentificationNumber())
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		return doGetRequest(requestTrackingId, uriBuilder);
	}

	public CloseableHttpResponse getDocument(final SenderId senderId, final long documentId, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId)
				.setParameter(AgreementType.QUERY_PARAM_NAME, AgreementType.INVOICE_BANK.getType());
		return doGetRequest(requestTrackingId, uriBuilder);
	}

	private CloseableHttpResponse doGetRequest(final String requestTrackingId, final URIBuilder uriBuilder) {
		HttpGet httpGet = new HttpGet(buildUri(uriBuilder));
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpGet, requestTrackingId);
		return send(httpGet);
	}

	public CloseableHttpResponse updateInvoice(final SenderId senderId, final long documentId, final Invoice invoice, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId + "/invoice");
		HttpPost httpPost = new HttpPost(buildUri(uriBuilder));
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_USERS_V1);
		addRequestTrackingHeader(httpPost, requestTrackingId);
		httpPost.setEntity(marshallJaxbEntity(invoice));
		return send(httpPost);
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
		return cachedEntryPoint.get();
	}

	private CloseableHttpResponse send(HttpRequestBase request) {
		try {
			request.setHeader(X_Digipost_UserId, brokerId.getIdAsString());
			return httpClient.execute(request);
		} catch (ClientProtocolException e) {
			throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST, e);
		} catch (IOException e) {
			throw new DigipostClientException(ErrorCode.CONNECTION_ERROR, e);
		}
	}

	private HttpRequestBase addRequestTrackingHeader(HttpRequestBase request, final String requestTrackingId) {
		if (requestTrackingId != null && requestTrackingId.length() > 0) {
			request.setHeader("X-Digipost-Request-Id", requestTrackingId);
		}
		return request;
	}

	private final Callable<EntryPoint> entryPoint = new Callable<EntryPoint>() {
		@Override
		public EntryPoint call() throws Exception {

			HttpGet httpGet = new HttpGet(serviceEndpoint.resolve(ROOT));
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

			try(CloseableHttpResponse execute = send(httpGet)) {
				if (execute.getStatusLine().getStatusCode() == SC_OK) {
					EntryPoint entryPoint = JAXB.unmarshal(execute.getEntity().getContent(), EntryPoint.class);
					return entryPoint;
				} else {
					ErrorMessage errorMessage = JAXB.unmarshal(execute.getEntity().getContent(), ErrorMessage.class);
					throw new DigipostClientException(errorMessage);
				}
			}

		}
	};

	private final SingleCached<EntryPoint> cachedEntryPoint = new SingleCached<>("digipost-entrypoint", entryPoint, expireAfterAccess(standardMinutes(5)), useSoftValues);
}
