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

import no.digipost.api.client.DigipostClientMock.ValidatingMarshaller;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.AuthorialSender;
import no.digipost.api.client.representations.sender.AuthorialSender.Type;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.representations.sender.SenderStatus;
import no.digipost.api.client.util.MockfriendlyResponse.MockedResponseBuilder;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.joda.time.DateTime;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.MessageStatus.COMPLETE;
import static no.digipost.api.client.representations.sender.SenderFeature.*;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

public class ApiServiceMock implements ApiService {

	private final EncryptionKey fakeEncryptionKey;

	public enum Method {
		SEND_MULTIPART_MESSAGE,
		GET_PRINT_KEY,
		GET_SENDER_INFORMATION,
		GET_CONTENT,
		MULTIPART_MESSAGE,
		GET_DOCUMENTS_EVENTS,
		GET_DOCUMENT_STATUS
	}

	final Map<Method, RequestsAndResponses> requestsAndResponsesMap = new HashMap<>();
	private final ValidatingMarshaller marshaller;
	private final long brokerId;

	public ApiServiceMock(ValidatingMarshaller validatingMarshaller) {
		this(42, validatingMarshaller);
	}
	public ApiServiceMock(long brokerId, ValidatingMarshaller validatingMarshaller) {
		this.marshaller = validatingMarshaller;
		this.fakeEncryptionKey = createFakeEncryptionKey();
		this.brokerId = brokerId;
		init();
	}

	static EncryptionKey createFakeEncryptionKey() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (Writer osWriter = new OutputStreamWriter(baos); JcaPEMWriter writer = new JcaPEMWriter(osWriter)) {

			KeyPairGenerator factory = KeyPairGenerator.getInstance("RSA");
			factory.initialize(2048);
			KeyPair keyPair = factory.generateKeyPair();

			writer.writeObject(keyPair.getPublic());

		} catch (Exception e) {
			throw new RuntimeException("Failed creation of fake encryption key.", e);
		}

		EncryptionKey fakeKey = new EncryptionKey();
		fakeKey.setKeyId("fake-hash");
		fakeKey.setValue(new String(baos.toByteArray()));

		return fakeKey;
	}

	private void init() {
		requestsAndResponsesMap.clear();
		requestsAndResponsesMap.put(Method.GET_CONTENT, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.GET_DOCUMENTS_EVENTS, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.GET_DOCUMENT_STATUS, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.MULTIPART_MESSAGE, new RequestsAndResponses(new MultipartRequestMatcher()));
	}

	public void reset() {
		init();
	}

	@Override
	public EntryPoint getEntryPoint() {
		throw new NotImplementedException("This is a mock");
	}


	@Override
	public CloseableHttpResponse multipartMessage(final HttpEntity multipart) {
		RequestsAndResponses requestsAndResponses = this.requestsAndResponsesMap.get(Method.MULTIPART_MESSAGE);
		CloseableHttpResponse response = requestsAndResponses.getResponse("Request");

		//TODO: Could be done a bit better
		return response;
	}

	@Override
	public CloseableHttpResponse identifyAndGetEncryptionKey(Identification identification) {
		IdentificationResultWithEncryptionKey mockEntity = new IdentificationResultWithEncryptionKey(
				IdentificationResult.digipost("fake.address#1234"),
				fakeEncryptionKey
		);

		org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
		JAXB.marshal(mockEntity, bao);

		return MockedResponseBuilder.create()
				.status(SC_OK)
				.entity(new ByteArrayEntity(bao.toByteArray()))
				.build();
	}

	@Override
	public CloseableHttpResponse getEncryptionKeyForPrint() {

		org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
		JAXB.marshal(fakeEncryptionKey, bao);

		return MockedResponseBuilder.create()
				.status(SC_OK)
				.entity(new ByteArrayEntity(bao.toByteArray()))
				.build();
	}

	@Override
	public CloseableHttpResponse createMessage(final Message message) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public CloseableHttpResponse fetchExistingMessage(final URI location) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public CloseableHttpResponse getEncryptionKey(final URI location) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public CloseableHttpResponse addContent(final Document document, final InputStream letterContent) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public CloseableHttpResponse send(final MessageDelivery createdMessage) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Recipients search(final String searchString) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Autocomplete searchSuggest(final String searchString) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public void addFilter(HttpResponseInterceptor interceptor) {
	}

	@Override
	public void addFilter(HttpRequestInterceptor interceptor) {
	}

	@Override
	public void buildApacheHttpClientBuilder() {
	}

	@Override
	public CloseableHttpResponse identifyRecipient(final Identification identification) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public CloseableHttpResponse getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to, final int offset, final int maxResults) {
		RequestsAndResponses requestsAndResponses = this.requestsAndResponsesMap.get(Method.GET_DOCUMENTS_EVENTS);
		CloseableHttpResponse response = requestsAndResponses.getResponse();

		org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
		JAXB.marshal(new DocumentEvents(), bao);

		if (response != null) {
			return response;
		} else {
			return MockedResponseBuilder.create().status(SC_OK).entity(new ByteArrayEntity(bao.toByteArray())).build();
		}
	}

	@Override
	public CloseableHttpResponse getDocumentStatus(Link linkToDocumentStatus) {
		return getDocumentStatus(1, "uuid");
	}

	@Override
	public CloseableHttpResponse getDocumentStatus(long senderId, String uuid) {
		RequestsAndResponses requestsAndResponses = this.requestsAndResponsesMap.get(Method.GET_DOCUMENT_STATUS);
		CloseableHttpResponse response = requestsAndResponses.getResponse();

		org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
		JAXB.marshal(new DocumentEvents(), bao);

		if (response != null) {
			return response;
		} else {
			return MockedResponseBuilder.create().status(SC_OK).entity(new ByteArrayEntity(bao.toByteArray())).build();
		}
	}

	@Override
	public CloseableHttpResponse getContent(String path) {
		RequestsAndResponses requestsAndResponses = this.requestsAndResponsesMap.get(Method.GET_CONTENT);
		CloseableHttpResponse response = requestsAndResponses.getResponse();

		if (response != null) {
			return response;
		} else {
			return MockedResponseBuilder.create().status(SC_NOT_FOUND).build();
		}
	}

	@Override
	public SenderInformation getSenderInformation(long senderId) {
		return new SenderInformation(senderId, SenderStatus.VALID_SENDER, asList(DIGIPOST_DELIVERY, DELIVERY_DIRECT_TO_PRINT, PRINTVALIDATION_FONTS, PRINTVALIDATION_PDFVERSION));
	}

	@Override
	public SenderInformation getSenderInformation(String orgnr, String avsenderenhet) {
		return getSenderInformation(brokerId);
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


	public static class RequestMatcher {
		public CloseableHttpResponse findResponse(String requestString) {
			return null;
		}
	}

	public static class RequestsAndResponses {
		private final Queue<ResponseProducer> responseQueue = new ConcurrentLinkedQueue<>();
		private final RequestMatcher requestMatcher;
		private final Map<String, MockRequest> requestMap;

		RequestsAndResponses() {
			this(new RequestMatcher());
		}

		RequestsAndResponses(RequestMatcher requestMatcher) {
			this.requestMatcher = requestMatcher;
			this.requestMap = Collections.synchronizedMap(new LinkedHashMap<String, MockRequest>() {
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, MockRequest> eldest) {
					return size() > 100;
				}
			});
		}

		public void addExpectedResponse(CloseableHttpResponse response) {
			responseQueue.add(new MockResponse(response));
		}
		public void addExpectedException(RuntimeException ex) {
			responseQueue.add(new MockResponse(ex));
		}

		public CloseableHttpResponse getResponse() {
			return getResponse("default");
		}

		public CloseableHttpResponse getResponse(String requestString) {
			ResponseProducer response = responseQueue.poll();
			if (response != null) {
				return response.getResponse();
			}

			return requestMatcher.findResponse(requestString);
		}

		public void addRequest(MockRequest request) {
			requestMap.put(request.getKey(), request);
		}

		public MockRequest getRequest(String requestKey) {
			return requestMap.get(requestKey);
		}

		public Map<String, MockRequest> getRequests() {
			return requestMap;
		}
	}

	public static interface ResponseProducer {
		CloseableHttpResponse getResponse();
	}
	public static class MockResponse implements ResponseProducer {

		private CloseableHttpResponse response;
		private RuntimeException exception;

		public MockResponse(CloseableHttpResponse response) {
			this.response = response;
		}
		public MockResponse(RuntimeException ex) {
			this.exception = ex;
		}

		@Override
		public CloseableHttpResponse getResponse() {
			if (exception != null) {
				throw exception;
			}
			return response;
		}
	}

	public static class MockRequest {
		private final String key;

		public MockRequest(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	public static class MultipartRequestMatcher extends RequestMatcher {

		public static CloseableHttpResponse DEFAULT_RESPONSE = getDefaultResponse();
		public static RuntimeException CONNECTION_REFUSED = new RuntimeException(new ConnectException("Connection refused"));

		public static final Map<String, CloseableHttpResponse> responses = new HashMap<>();
		public static final Map<String, RuntimeException> errors = new HashMap<>();

		public static CloseableHttpResponse getDefaultResponse(){
			MessageDelivery messageDelivery = new MessageDelivery(UUID.randomUUID().toString(), Channel.DIGIPOST, COMPLETE, DateTime.now());

			org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
			JAXB.marshal(messageDelivery, bao);

			return MockedResponseBuilder.create()
					.status(SC_OK)
					.entity(new ByteArrayEntity(bao.toByteArray()))
					.build();
		}


		static {
			responses.put("200:OK", DEFAULT_RESPONSE);
			errors.put("CONNECTION_REFUSED", CONNECTION_REFUSED);
		}

		@Override
		public CloseableHttpResponse findResponse(String requestString) {

			if (responses.containsKey(requestString)) {
				return responses.get(requestString);
			} else if (errors.containsKey(requestString)) {
				throw errors.get(requestString);
			} else if (requestString.matches("^[0-9]{3}:(.)+")) {
				String[] split = requestString.split(":");
				if (ErrorCode.isKnown(split[1])) {
					ErrorCode errorCode = ErrorCode.resolve(split[1]);
					ErrorType translated = EnumUtils.getEnum(ErrorType.class, errorCode.getOverriddenErrorType().name());

					org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
					JAXB.marshal(new ErrorMessage(translated != null ? translated : ErrorType.SERVER, errorCode.name(), "Generic error-message from digipost-api-client-mock"), bao);

					return MockedResponseBuilder.create().status(parseInt(split[0]))
							.entity(new ByteArrayEntity(bao.toByteArray())).build();
				} else {
					throw new IllegalArgumentException("ErrorCode " + split[1] + " is unknown");
				}
			} else {
				return DEFAULT_RESPONSE;
			}

		}
	}

	public static class DigipostRequest extends ApiServiceMock.MockRequest {
		public final Message message;
		public final List<ContentPart> contentParts;

		public DigipostRequest(Message message, List<ContentPart> contentParts) {
			super(message.messageId);
			this.message = message;
			this.contentParts = contentParts;
		}

	}

	public static class ContentPart {

		public final String mediaType;

		public ContentPart(String mediaType) {
			this.mediaType = mediaType;
		}
	}
}
