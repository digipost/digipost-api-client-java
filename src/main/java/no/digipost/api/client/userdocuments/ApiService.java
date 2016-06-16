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
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.cache.inmemory.SingleCached;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import static no.digipost.api.client.Headers.X_Digipost_UserId;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V6;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V7;
import static no.digipost.cache.inmemory.CacheConfig.expireAfterAccess;
import static no.digipost.cache.inmemory.CacheConfig.useSoftValues;
import static org.apache.http.HttpStatus.SC_OK;
import static org.joda.time.Duration.standardMinutes;

public class ApiService {

	private static final String ROOT = "/";
	private static final String USER_DOCUMENTS = "/user-documents";
	private static final String USER_AGREEMENTS = "/user-agreements";

	private final URI serviceEndpoint;
	private final long accountId;
	private final CloseableHttpClient httpClient;

	public ApiService(final URI serviceEndpoint, final long accountId, final CloseableHttpClient httpClient) {
		this.serviceEndpoint = serviceEndpoint;
		this.accountId = accountId;
		this.httpClient = httpClient;
	}

	public CloseableHttpResponse identifyUser(final UserId userId) {
		HttpPost httpPost = prepareHttpPost(getEntryPoint().getIdentificationUri().getPath());
		httpPost.setEntity(marshallJaxbEntity(new Identification(new PersonalIdentificationNumber(userId.getFnr()))));
		return send(httpPost);
	}

	public CloseableHttpResponse createAgreement(final Agreement agreement) {
		HttpPost httpPost = prepareHttpPost(USER_AGREEMENTS);
		httpPost.setEntity(marshallJaxbEntity(agreement));
		return send(httpPost);
	}

	public CloseableHttpResponse getAgreements(final UserId userId) {
		try {
			URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
					.setPath(USER_AGREEMENTS)
					.setParameter("user-id", userId.getFnr());
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V7);
			return send(httpGet);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public CloseableHttpResponse getDocuments(final UserId userId, final AgreementType agreementType) {
		try {
			URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
					.setPath(USER_DOCUMENTS)
					.setParameter(UserId.QUERY_PARAM_NAME, userId.getFnr())
					.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V7);
			return send(httpGet);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private HttpPost prepareHttpPost(final String path) {
		HttpPost httpPost = new HttpPost(serviceEndpoint.resolve(path));
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V7);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V7);
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

	private CloseableHttpResponse send(HttpRequestBase request){
		try {
			request.setHeader(X_Digipost_UserId, String.valueOf(accountId));
			return httpClient.execute(request);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
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
