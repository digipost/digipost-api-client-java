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

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.AuthorialSender;
import no.digipost.api.client.representations.sender.AuthorialSender.Type;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.util.MultipartNoLengthCheckHttpEntity;
import no.digipost.cache.inmemory.Cache;
import no.digipost.cache.inmemory.SingleCached;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import static no.digipost.api.client.Headers.X_Digipost_UserId;
import static no.digipost.api.client.errorhandling.ErrorCode.PROBLEM_WITH_REQUEST;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V6;
import static no.digipost.cache.inmemory.CacheConfig.expireAfterAccess;
import static no.digipost.cache.inmemory.CacheConfig.useSoftValues;
import static no.motif.Singular.optional;
import static no.motif.Strings.prepend;
import static org.joda.time.Duration.standardMinutes;

public class ApiServiceImpl implements ApiService {

	private static final String ENTRY_POINT = "/";
	private final long brokerId;
	private CloseableHttpClient httpClient;
	private final String digipostUrl;
	private final RequestConfig config;
	private final HttpClientBuilder httpClientBuilder;

	private final Callable<EntryPoint> entryPoint = new Callable<EntryPoint>() {
		@Override
        public EntryPoint call() throws Exception {

			HttpGet httpGet = new HttpGet(digipostUrl + ENTRY_POINT);
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

			try(CloseableHttpResponse execute = send(httpGet)) {

				if (execute.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
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
	private final Cache<String, SenderInformation> senderInformation = new Cache<>("sender-information", expireAfterAccess(standardMinutes(5)), useSoftValues);
	private final EventLogger eventLogger;

	public ApiServiceImpl(HttpClientBuilder httpClientBuilder, long senderAccountId, EventLogger eventLogger, String digipostUrl,
						  HttpHost proxy) {
		this.brokerId = senderAccountId;
		this.eventLogger = eventLogger;
		this.digipostUrl = digipostUrl;
		if(proxy != null) {
			this.config = RequestConfig.custom()
					.setProxy(proxy)
					.build();
		} else {
			this.config = null;
		}

		this.httpClientBuilder = httpClientBuilder;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return cachedEntryPoint.get();
	}


	@Override
	public CloseableHttpResponse multipartMessage(final HttpEntity multipart) {
		MultipartNoLengthCheckHttpEntity multipartLengthCheckHttpEntity = new MultipartNoLengthCheckHttpEntity(multipart);

		EntryPoint entryPoint = getEntryPoint();

		HttpPost httpPost = new HttpPost(digipostUrl + entryPoint.getCreateMessageUri().getPath());
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		httpPost.setHeader("MIME-Version", "1.0");
		httpPost.removeHeaders("Accept-Encoding");
		httpPost.setEntity(multipartLengthCheckHttpEntity);
		return send(httpPost);

	}

	@Override
	public CloseableHttpResponse identifyAndGetEncryptionKey(final Identification identification) {
		EntryPoint entryPoint = getEntryPoint();

		HttpPost httpPost = new HttpPost(digipostUrl + entryPoint.getIdentificationWithEncryptionKeyUri().getPath());
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V6);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXB.marshal(identification, bao);
		httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
		return send(httpPost);
	}

	@Override
	public CloseableHttpResponse createMessage(final Message message) {

		EntryPoint entryPoint = getEntryPoint();

		HttpPost httpPost = new HttpPost(digipostUrl + entryPoint.getCreateMessageUri().getPath());
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V6);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXB.marshal(message, bao);
		httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
		return send(httpPost);
	}

	@Override
	public CloseableHttpResponse fetchExistingMessage(final URI location) {
		HttpGet httpGet = new HttpGet(digipostUrl + location.getPath());
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		return send(httpGet);
	}

	@Override
	public CloseableHttpResponse getEncryptionKey(final URI location) {
		HttpGet httpGet = new HttpGet(location);
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		return send(httpGet);
	}

	@Override
	public CloseableHttpResponse getEncryptionKeyForPrint() {
		EntryPoint entryPoint = getEntryPoint();

		HttpGet httpGet = new HttpGet(digipostUrl + entryPoint.getPrintEncryptionKey().getPath());
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		return send(httpGet);
	}

	@Override
	public CloseableHttpResponse addContent(final Document document, final InputStream letterContent) {
		Link addContentLink = fetchAddContentLink(document);

		byte[] content = readLetterContent(letterContent);

		HttpPost httpPost = new HttpPost(digipostUrl + addContentLink.getUri().getPath());
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString());
		httpPost.setEntity(new ByteArrayEntity(content));
		return send(httpPost);
	}

	@Override
	public CloseableHttpResponse send(final MessageDelivery createdMessage) {
		Link sendLink = fetchSendLink(createdMessage);

		HttpPost httpPost = new HttpPost(digipostUrl + sendLink.getUri().getPath());
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		httpPost.setEntity(null);
		return send(httpPost);

	}

	private Link fetchAddContentLink(final Document document) {
		Link addContentLink = document.getAddContentLink();
		if (addContentLink == null) {
			throw new DigipostClientException(PROBLEM_WITH_REQUEST,
					"Kan ikke legge til innhold til et dokument som ikke har en link for å gjøre dette.");
		}
		return addContentLink;
	}

	private Link fetchSendLink(final MessageDelivery delivery) {
		Link sendLink = delivery.getSendLink();
		if (sendLink == null) {
			throw new DigipostClientException(PROBLEM_WITH_REQUEST,
					"Kan ikke sende en forsendelse som ikke har en link for å gjøre dette.");
		}
		return sendLink;
	}


	byte[] readLetterContent(final InputStream letterContent) {
		try {
			return IOUtils.toByteArray(letterContent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CloseableHttpResponse getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to, final int offset, final int maxResults) {

		URIBuilder builder = new URIBuilder().setPath(digipostUrl + getEntryPoint().getDocumentEventsUri().getPath())
				.setParameter("from", from.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
				.setParameter("to", to.toString("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
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
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			return send(httpGet);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse getDocumentStatus(Link linkToDocumentStatus) {
		return getDocumentStatus(linkToDocumentStatus.getUri().getPath());
	}

	@Override
	public CloseableHttpResponse getDocumentStatus(long senderId, String uuid) {
		return getDocumentStatus(String.format("/documents/%s/%s/status", senderId, uuid));
	}

	private CloseableHttpResponse getDocumentStatus(String path) {

		HttpGet httpGet = new HttpGet(digipostUrl + path);
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

		return send(httpGet);
	}

	@Override
	public CloseableHttpResponse getContent(String path) {
		HttpGet httpGet = new HttpGet(digipostUrl + path);

		return send(httpGet);
	}

	@Override
	public Recipients search(final String searchString) {
		HttpGet httpGet = new HttpGet(digipostUrl + getEntryPoint().getSearchUri().getPath() + "/" + searchString);
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

		try(CloseableHttpResponse response = send(httpGet)){
			Recipients recipients = JAXB.unmarshal(response.getEntity().getContent(), Recipients.class);
			return recipients;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public Autocomplete searchSuggest(final String searchString) {
		HttpGet httpGet = new HttpGet(digipostUrl + getEntryPoint().getAutocompleteUri().getPath() + "/" + searchString);
		httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);


		try(CloseableHttpResponse response = send(httpGet);) {
			Autocomplete autocomplete = JAXB.unmarshal(response.getEntity().getContent(), Autocomplete.class);
			return  autocomplete;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void addFilter(HttpResponseInterceptor interceptor) {
		httpClientBuilder.addInterceptorLast(interceptor);
	}

	@Override
	public void addFilter(HttpRequestInterceptor interceptor) {
		httpClientBuilder.addInterceptorLast(interceptor);
	}

	@Override
	public void buildApacheHttpClientBuilder(){
		httpClient = httpClientBuilder.build();
	}

	@Override
	public CloseableHttpResponse identifyRecipient(final Identification identification) {

		HttpPost httpPost = new HttpPost(digipostUrl + getEntryPoint().getIdentificationUri().getPath());
		httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V6);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXB.marshal(identification, bao);
		httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));

		return send(httpPost);
	}

	private CloseableHttpResponse send(HttpRequestBase request){
		try {
			if(config != null){
				request.setConfig(config);
			}
			request.setHeader(X_Digipost_UserId, brokerId + "");
			return httpClient.execute(request);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	@Override
    public SenderInformation getSenderInformation(long senderId) {
		return senderInformation.get(String.valueOf(senderId),
				getResource(getEntryPoint().getSenderInformationUri().getPath() + "/" + senderId, SenderInformation.class));
    }

	@Override
    public SenderInformation getSenderInformation(String orgnr, String avsenderenhet) {
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("org_id", orgnr);
		if (avsenderenhet != null) {
			queryParams.put("part_id", avsenderenhet);
		}

		return senderInformation.get(orgnr + optional(avsenderenhet).map(prepend("-")).orElse(""),
				getResource(getEntryPoint().getSenderInformationUri().getPath(), queryParams, SenderInformation.class));
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


	private <R> Callable<R> getResource(final String path, final Class<R> entityType) {
		return getResource(path, new HashMap<String, Object>(), entityType);
	}

	private <R, P> Callable<R> getResource(final String path, final Map<String, P> queryParams, final Class<R> entityType) {
		return new Callable<R>() {
			@Override
            public R call() {

				try {
					HttpGet httpGet = new HttpGet(digipostUrl + path);
					URIBuilder uriBuilder = new URIBuilder(httpGet.getURI());

					for (Entry<String, P> param : queryParams.entrySet()) {
						uriBuilder.setParameter(param.getKey(), param.getValue().toString());
					}

					httpGet.setURI(uriBuilder.build());
					httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

					try (CloseableHttpResponse execute = send(httpGet)){
						Communicator.checkResponse(execute, eventLogger);
						R unmarshal = JAXB.unmarshal(execute.getEntity().getContent(), entityType);
						return unmarshal;

					} catch (IOException e) {
						throw new RuntimeException(e.getMessage(), e);
					}
				} catch (URISyntaxException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
            }
		};
	}

}
