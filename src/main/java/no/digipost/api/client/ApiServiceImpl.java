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
import no.digipost.cache.inmemory.Cache;
import no.digipost.cache.inmemory.SingleCached;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.joda.time.DateTime;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXB;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
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


	private final Callable<EntryPoint> entryPoint = new Callable<EntryPoint>() {
		@Override
        public EntryPoint call() throws Exception {
			HttpGet httpGet = new HttpGet(digipostUrl + ENTRY_POINT);
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

			CloseableHttpResponse execute = httpClient.execute(httpGet);

			execute.close();

			if(execute.getStatusLine().getStatusCode() == OK.getStatusCode()){
				EntryPoint entryPoint = JAXB.unmarshal(execute.getEntity().getContent(), EntryPoint.class);
				execute.close();
				return entryPoint;
			} else {
				ErrorMessage errorMessage = JAXB.unmarshal(execute.getEntity().getContent(), ErrorMessage.class);
				execute.close();
				throw new DigipostClientException(errorMessage);
			}

        }
	};

	private final SingleCached<EntryPoint> cachedEntryPoint = new SingleCached<>("digipost-entrypoint", entryPoint, expireAfterAccess(standardMinutes(5)), useSoftValues);
	private final Cache<String, SenderInformation> senderInformation = new Cache<>("sender-information", expireAfterAccess(standardMinutes(5)), useSoftValues);
	private final EventLogger eventLogger;

	public ApiServiceImpl(long senderAccountId, EventLogger eventLogger, String digipostUrl) {
		this.brokerId = senderAccountId;
		this.eventLogger = eventLogger;
		this.digipostUrl = digipostUrl;
	}

	public void setApacheClient(CloseableHttpClient httpClient){
		this.httpClient = httpClient;
	}

	@Override
	public EntryPoint getEntryPoint() {
		return cachedEntryPoint.get();
	}


	@Override
	public CloseableHttpResponse multipartMessage(final HttpEntity multipart) {
		EntryPoint entryPoint = getEntryPoint();

		HttpPost httpPost = new HttpPost(digipostUrl + entryPoint.getCreateMessageUri().getPath());
		try {
			httpPost.addHeader(X_Digipost_UserId, brokerId + "");
			httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			httpPost.setHeader("MIME-Version", "1.0");
			httpPost.removeHeaders("Accept-Encoding");
			httpPost.setEntity(multipart);
			return httpClient.execute(httpPost);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse identifyAndGetEncryptionKey(final Identification identification) {
		EntryPoint entryPoint = getEntryPoint();

		HttpPost httpPost = new HttpPost(digipostUrl + entryPoint.getIdentificationWithEncryptionKeyUri().getPath());
		try {
			httpPost.addHeader(X_Digipost_UserId, brokerId + "");
			httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V6);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			JAXB.marshal(identification, bao);
			httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
			return httpClient.execute(httpPost);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse createMessage(final Message message) {

		EntryPoint entryPoint = getEntryPoint();

		HttpPost httpPost = new HttpPost(digipostUrl + entryPoint.getCreateMessageUri().getPath());
		try {
			httpPost.addHeader(X_Digipost_UserId, brokerId + "");
			httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V6);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			JAXB.marshal(message, bao);
			httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
			return httpClient.execute(httpPost);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse fetchExistingMessage(final URI location) {
		HttpGet httpGet = new HttpGet(digipostUrl + location.getPath());
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			return httpClient.execute(httpGet);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse getEncryptionKey(final URI location) {
		HttpGet httpGet = new HttpGet(location);
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			return httpClient.execute(httpGet);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse getEncryptionKeyForPrint() {
		EntryPoint entryPoint = getEntryPoint();

		HttpGet httpGet = new HttpGet(digipostUrl + entryPoint.getPrintEncryptionKey().getPath());
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			return httpClient.execute(httpGet);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse addContent(final Document document, final InputStream letterContent) {
		Link addContentLink = fetchAddContentLink(document);

		byte[] content = readLetterContent(letterContent);

		HttpPost httpPost = new HttpPost(digipostUrl + addContentLink.getUri().getPath());
		try {
			httpPost.addHeader(X_Digipost_UserId, brokerId + "");
			httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_OCTET_STREAM_TYPE.toString());
			httpPost.setEntity(new ByteArrayEntity(content));
			return httpClient.execute(httpPost);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	@Override
	public CloseableHttpResponse send(final MessageDelivery createdMessage) {
		Link sendLink = fetchSendLink(createdMessage);

		HttpPost httpPost = new HttpPost(digipostUrl + sendLink.getUri().getPath());
		try {
			httpPost.addHeader(X_Digipost_UserId, brokerId + "");
			httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			httpPost.setEntity(null);
			return httpClient.execute(httpPost);

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
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
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			return httpClient.execute(httpGet);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
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
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			return httpClient.execute(httpGet);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public CloseableHttpResponse getContent(String path) {
		HttpGet httpGet = new HttpGet(digipostUrl + path);
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			return httpClient.execute(httpGet);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		/*return webResource
				.path(path)
				.request()
				.header(X_Digipost_UserId, brokerId)
				.get();*/
	}

	@Override
	public Recipients search(final String searchString) {
		HttpGet httpGet = new HttpGet(digipostUrl + getEntryPoint().getSearchUri().getPath() + "/" + searchString);
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			Recipients recipients = JAXB.unmarshal(response.getEntity().getContent(), Recipients.class);
			response.close();
			return recipients;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public Autocomplete searchSuggest(final String searchString) {
		HttpGet httpGet = new HttpGet(digipostUrl + getEntryPoint().getAutocompleteUri().getPath() + "/" + searchString);
		try {
			httpGet.addHeader(X_Digipost_UserId, brokerId + "");
			httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			Autocomplete autocomplete = JAXB.unmarshal(response.getEntity().getContent(), Autocomplete.class);
			response.close();
			return autocomplete;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void addFilter(final ClientRequestFilter filter) {
		//TODO what to do with this
		//webResource.register(filter);
	}

	@Override
	public CloseableHttpResponse identifyRecipient(final Identification identification) {

		HttpPost httpPost = new HttpPost(digipostUrl + getEntryPoint().getIdentificationUri().getPath());
		try {
			httpPost.addHeader(X_Digipost_UserId, brokerId + "");
			httpPost.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V6);
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			JAXB.marshal(identification, bao);
			httpPost.setEntity(new ByteArrayEntity(bao.toByteArray()));
			return httpClient.execute(httpPost);
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
		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
		queryParams.putSingle("org_id", orgnr);
		if (avsenderenhet != null) {
			queryParams.putSingle("part_id", avsenderenhet);
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
		return getResource(path, new MultivaluedHashMap<String, Object>(), entityType);
	}

	private <R, P> Callable<R> getResource(final String path, final MultivaluedMap<String, P> queryParams, final Class<R> entityType) {
		return new Callable<R>() {
			@Override
            public R call() {
				HttpGet httpGet = new HttpGet(digipostUrl + path);
				try {
					for (Entry<String, List<P>> param : queryParams.entrySet()) {
						httpGet.addHeader(param.getKey(), param.getValue().toString());
					}
					httpGet.addHeader(X_Digipost_UserId, brokerId + "");
					httpGet.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_V6);

					CloseableHttpResponse execute = httpClient.execute(httpGet);
					Communicator.checkResponse(execute, eventLogger);
					R unmarshal = JAXB.unmarshal(execute.getEntity().getContent(), entityType);
					execute.close();
					return unmarshal;

				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
            }
		};
	}

}
