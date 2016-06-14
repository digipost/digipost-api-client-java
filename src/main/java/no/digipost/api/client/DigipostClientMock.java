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

import io.undertow.Undertow;
import no.digipost.api.client.ApiServiceMock.Method;
import no.digipost.api.client.ApiServiceMock.MultipartRequestMatcher;
import no.digipost.api.client.ApiServiceMock.RequestsAndResponses;
import no.digipost.api.client.delivery.ApiFlavor;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.util.DigipostApiMock;
import no.digipost.http.client.DigipostHttpClientFactory;
import no.digipost.http.client.DigipostHttpClientSettings;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xml.sax.ContentHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.*;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import static no.digipost.api.client.ApiServiceMock.MockRequest;
import static no.digipost.api.client.DigipostClientConfig.DigipostClientConfigBuilder.newBuilder;

/**
 * Instansierer en DigipostClient som ikke går mot faktiskt Digipost REST-api endepunkt og
 * som kan brukes av tester o.l for å framprovosere ulike feilsituasjoner.
 */
public class DigipostClientMock {

	private final DigipostClient client;
	private final ApiService apiService;
	public final Map<Method, RequestsAndResponses> requestsAndResponsesMap = new HashMap<>();
	private static DigipostApiMock digipostApiMock = new DigipostApiMock();
	private static final String KEY_STORE_PASSWORD = "Qwer12345";
	private static final String KEY_STORE_ALIAS = "apiTest";

	public DigipostClientMock(ApiFlavor apiFlavor) {
		if (apiFlavor == ApiFlavor.STEPWISE_REST) {
			throw new RuntimeException("Stepwise REST is not yet supported by " + DigipostClientMock.class.getName());
		}

		int port = 9999;
		String host = "http://localhost:" + port;

		KeyPair keyPair = getKeyPair(KEY_STORE_ALIAS, KEY_STORE_PASSWORD);

		init();
		digipostApiMock.start(port, requestsAndResponsesMap, keyPair);

		HttpClientBuilder httpClientBuilder = DigipostHttpClientFactory.createBuilder(DigipostHttpClientSettings.DEFAULT);

		apiService = new ApiServiceImpl(httpClientBuilder, port, null, host);
		apiService.buildApacheHttpClientBuilder();
		client = new DigipostClient(newBuilder().build(),apiFlavor, "digipostmock-url", 1, new Signer() {

			@Override
			public byte[] sign(String dataToSign) {
				return new byte[0];
			}
		}, apiService);
	}

	public static KeyPair getKeyPair(final String alias, final String password) {
		try {
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(DigipostClientMock.class.getClass().getResourceAsStream("/mockKeystore.jks"), KEY_STORE_PASSWORD.toCharArray());

			final Key key = (PrivateKey) keystore.getKey(alias, password.toCharArray());
			final Certificate cert = keystore.getCertificate(alias);
			final PublicKey publicKey = cert.getPublicKey();

			return new KeyPair(publicKey, (PrivateKey) key);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void shutdownWebserver(){
		digipostApiMock.stop();
	}

	public DigipostClient getClient() {
		return client;
	}

	public Map<String, MockRequest> getAllRequests(Method method) {
		return requestsAndResponsesMap.get(method).getRequests();
	}

	public MockRequest getRequest(Method method, String requestKey) {
		return requestsAndResponsesMap.get(method).getRequest(requestKey);
	}

	public void addExpectedResponse(Method method, CloseableHttpResponse response) {
		ApiServiceMock.RequestsAndResponses requestsAndResponses = requestsAndResponsesMap.get(method);

		requestsAndResponses.addExpectedResponse(response);
	}

	public void addExpectedException(Method method, RuntimeException exception) {
		ApiServiceMock.RequestsAndResponses requestsAndResponses = requestsAndResponsesMap.get(method);

		requestsAndResponses.addExpectedException(exception);
	}

	public void reset() {
		init();
	}

	private void init() {
		requestsAndResponsesMap.clear();
		requestsAndResponsesMap.put(Method.GET_CONTENT, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.GET_DOCUMENTS_EVENTS, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.GET_DOCUMENT_STATUS, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.GET_PRINT_KEY, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.GET_SENDER_INFORMATION, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.SEND_MULTIPART_MESSAGE, new RequestsAndResponses());
		requestsAndResponsesMap.put(Method.MULTIPART_MESSAGE, new RequestsAndResponses(new MultipartRequestMatcher()));
	}

	/**
	 * Threadsafe instance for marshalling and validating.
	 */
	public static class ValidatingMarshaller {
		private final JAXBContext jaxbContext;
		private final Schema schema;

		public ValidatingMarshaller(JAXBContext jaxbContext, Schema schema) {
			this.jaxbContext = jaxbContext;
			this.schema = schema;
		}

		public void marshal(Object jaxbElement, ContentHandler handler) {
			Marshaller marshaller;
			try {
				marshaller = jaxbContext.createMarshaller();
				marshaller.setSchema(schema);
				marshaller.marshal(jaxbElement, handler);
			} catch (JAXBException e) {
				StringWriter w = new StringWriter();
				PrintWriter printWriter = new PrintWriter(w);
				e.printStackTrace(printWriter);
				throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST, "DigipostClientMock failed to marshall the " + jaxbElement.getClass().getSimpleName() + " to xml.\n\n" + w.toString());
			}
		}
	}

	/**
	 * Used to validate that requests are according to XSD
	 */
	public static ValidatingMarshaller initMarshaller() {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(DigipostClient.class.getResource("/xsd/api_v6.xsd"));
			return new ValidatingMarshaller(JAXBContext.newInstance("no.digipost.api.client.representations"), schema);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
