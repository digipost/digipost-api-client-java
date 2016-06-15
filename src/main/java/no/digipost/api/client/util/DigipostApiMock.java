package no.digipost.api.client.util;
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
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import no.digipost.api.client.ApiServiceMock;
import no.digipost.api.client.ApiServiceMock.Method;
import no.digipost.api.client.ApiServiceMock.RequestsAndResponses;
import no.digipost.api.client.Headers;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderFeature;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.representations.sender.SenderStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import static org.joda.time.DateTime.*;

public class DigipostApiMock implements HttpHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DigipostApiMock.class);

	private static final String PRINT_KEY = "-----BEGIN PUBLIC KEY-----\n" +
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA+kcLpddPWKYlmIPZSDtw\n" +
			"9B/k+PFcCqnjh2xdhSkmYh+FQsYts0U1WCA8s7NPDUM7qwa97jlQ+la5AAxprfJQ\n" +
			"YOAp6NI5yLRuiah3EdnbPOyTQJ/C7hiw0P1lK0301BEMB2oHIxGYjUTpGeLLm2tH\n" +
			"z9VfJzkefCq1W/KV63AIDWDopwnw8UdhOgtB3BY+fVWVsv8WtG3kbfVn7t+7A5M9\n" +
			"ueE0BGGzL7XHCwBHJDq8+D0qNNvKIjp6KSgb+ZaREQEgCujuL77Qtm+6zKCpJXi2\n" +
			"yF+7mEaHj5k4pSnzI/N7CyDK1s24CeRpfDT0o8dz2O0VmV1ukA7wAqVJk2tKlhqE\n" +
			"IwIDAQAB\n" +
			"-----END PUBLIC KEY-----\n";

	private static final String PRINT_ID = "81bc03451048fba4d31f8";

	private static final String certificate = "-----BEGIN CERTIFICATE-----\n" +
			"MIIDczCCAlugAwIBAgIEfMS/vzANBgkqhkiG9w0BAQsFADBqMQswCQYDVQQGEwJO\n" +
			"TzENMAsGA1UECBMET3NsbzENMAsGA1UEBxMET3NsbzERMA8GA1UEChMIRGlnaXBv\n" +
			"c3QxEDAOBgNVBAsTB0RpZ2lwb3MxGDAWBgNVBAMTD1NpbW9uIEFuZGVyc3NvbjAe\n" +
			"Fw0xNjA2MTMxMDE5MTJaFw0xNjA5MTExMDE5MTJaMGoxCzAJBgNVBAYTAk5PMQ0w\n" +
			"CwYDVQQIEwRPc2xvMQ0wCwYDVQQHEwRPc2xvMREwDwYDVQQKEwhEaWdpcG9zdDEQ\n" +
			"MA4GA1UECxMHRGlnaXBvczEYMBYGA1UEAxMPU2ltb24gQW5kZXJzc29uMIIBIjAN\n" +
			"BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgTSx+3/K/AOpG1TGcnceq6H1FNp1\n" +
			"VseUULBFhT7wmodEhijI5RPOkG0Okt0GA6yT6BNS7xhPrxLROSnFPCURtq/h13Gf\n" +
			"O/5DKUqoofIzP2f26s96GNwlUHLTnnGQ1mq2LTVu8Hi3vXwXjeBWoHSVceF8nPZr\n" +
			"YWm4Cxqn09dxWv/FuOqX1JwLwWBGgkyONohdmFaHFg+MRHzArvkdBM8sleD8Cwyx\n" +
			"ASlB7+9ttVZrssg2IxZ6wBEbkulGjLOrOa//dxwlFpEww9pm89hRIdj9sRUScbCO\n" +
			"Hto7KUMbbhZgiaDvCjIfOVeGWCJKyMF7avPk/86z11F5rjF33JieItn6RwIDAQAB\n" +
			"oyEwHzAdBgNVHQ4EFgQU/bPHm6m6ISWYU0DOUw8KqjEF8w4wDQYJKoZIhvcNAQEL\n" +
			"BQADggEBAEj05cTCWZuzhjKyOx2SvLc40WHPkTZCLx+4LNZHdckKgdGMcJJfqYr+\n" +
			"eTbsdzBgmFa1wMO0H6fKcaEgiJj2G9B4no/7bUWY2q3I21ZprJpiJnvuk36blcw1\n" +
			"s7jaDFQ758fyqAV/Sqqz91gSKPRsKCtcHC72XEqdc40qcecY1+3OVg5rADJHC8u+\n" +
			"QaXy0670ViiULi3vmQNAtD28DZcOHbbplVXKSyVifnALTrCQBYMqQF8GpKs2NcId\n" +
			"0XUU7HFKXmYESXGY4sW77+l8mRXLcpD/ofAszblUPpPCFFWozH4/WoY7tDRDhwkz\n" +
			"6vYEpI930hzj81irYEPyRvoDAaEv2Zc=\n" +
			"-----END CERTIFICATE-----";


	private byte[] stubbedResponse;
	private int port;
	private Undertow server;
	private final BlockingDeque<ReceivedRequest> receivedRequests = new LinkedBlockingDeque<>();
	private Map<Method, RequestsAndResponses> requestsAndResponsesMap;
	private KeyPair keyPair;

	@Override
	public void handleRequest(HttpServerExchange httpContext) throws Exception {
		String httpResponse = "200";
		HttpString method = httpContext.getRequestMethod();

		ByteArrayOutputStream bao = new ByteArrayOutputStream();

		if(method.equals(new HttpString("POST"))){
			String multipart = new String(IOUtils.toByteArray(httpContext.getInputStream()));
			String messageString = multipart.substring(multipart.indexOf("<?xml"));
			messageString = messageString.substring(0, messageString.indexOf("</message>") + 10);

			ByteArrayInputStream messageStream = new ByteArrayInputStream(messageString.getBytes());

			Message message = JAXB.unmarshal(messageStream, Message.class);

			RequestsAndResponses requestsAndResponses = this.requestsAndResponsesMap.get(Method.MULTIPART_MESSAGE);
			CloseableHttpResponse response = requestsAndResponses.getResponse(message.primaryDocument.subject);

			httpContext.setStatusCode(response.getStatusLine().getStatusCode());
			httpResponse = response.getStatusLine().getStatusCode() + "";
			response.getEntity().writeTo(bao);

			requestsAndResponses.addRequest(new ApiServiceMock.DigipostRequest(message, getContentparts(multipart)));

		} else if(httpContext.getRequestPath().toLowerCase().equals("/printkey")) {
			CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_PRINT_KEY).getResponse(httpContext.getRequestPath());
			EncryptionKey encryptionKey = new EncryptionKey();
			encryptionKey.setKeyId(PRINT_ID);
			encryptionKey.setValue(PRINT_KEY);
			JAXB.marshal(encryptionKey, bao);

		} else if(httpContext.getRequestPath().toLowerCase().equals("/getsenderinformation")) {
			CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_SENDER_INFORMATION).getResponse(httpContext.getRequestPath());
			if(response == null) {
				List<SenderFeature> senderFeatures = new ArrayList<>();
				senderFeatures.add(SenderFeature.DELIVERY_DIRECT_TO_PRINT);
				senderFeatures.add(SenderFeature.DIGIPOST_DELIVERY);
				senderFeatures.add(SenderFeature.PRINTVALIDATION_FONTS);
				senderFeatures.add(SenderFeature.PRINTVALIDATION_PDFVERSION);
				JAXB.marshal(new SenderInformation(9999L, SenderStatus.VALID_SENDER, senderFeatures), bao);
			} else {
				httpContext.setStatusCode(response.getStatusLine().getStatusCode());
				httpResponse = response.getStatusLine().getStatusCode() + "";
				response.getEntity().writeTo(bao);
			}

		} else if(httpContext.getRequestPath().toLowerCase().equals("/getdocumentstatus")) {
			CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_DOCUMENT_STATUS).getResponse(httpContext.getRequestPath());
			httpContext.setStatusCode(response.getStatusLine().getStatusCode());
			httpResponse = response.getStatusLine().getStatusCode() + "";
			response.getEntity().writeTo(bao);

		} else if(httpContext.getRequestPath().toLowerCase().equals("/getdocumentevents")) {
			CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_DOCUMENTS_EVENTS).getResponse(httpContext.getRequestPath());
			httpContext.setStatusCode(response.getStatusLine().getStatusCode());
			httpResponse = response.getStatusLine().getStatusCode() + "";
			response.getEntity().writeTo(bao);

		} else if(httpContext.getRequestPath().toLowerCase().equals("/")){
			JAXB.marshal(new EntryPoint(certificate, new Link(Relation.CREATE_MESSAGE, new DigipostUri("http://localhost:9999/create")),
					new Link(Relation.GET_PRINT_ENCRYPTION_KEY, new DigipostUri("http://localhost:9999/printkey")),
					new Link(Relation.GET_SENDER_INFORMATION, new DigipostUri("http://localhost:9999/getsenderinformation")),
					new Link(Relation.DOCUMENT_EVENTS, new DigipostUri("http://localhost:9999/getdocumentevents"))), bao);

		} else {
			CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_CONTENT).getResponse(httpContext.getRequestPath());
			httpContext.setStatusCode(response.getStatusLine().getStatusCode());
			httpResponse = response.getStatusLine().getStatusCode() + "";
			response.getEntity().writeTo(bao);
		}

		byte[] bytes = bao.toByteArray();
		HeaderMap responseHeaders = httpContext.getResponseHeaders();
		String dateOnRFC1123Format = DateUtils.formatDate(now());

		responseHeaders.add(new HttpString("Date"), dateOnRFC1123Format);
		String xContentSHA256 = generateXContentSHA256(bytes);
		responseHeaders.add(new HttpString(Headers.X_Content_SHA256), xContentSHA256);

		String signature =  httpResponse + "\n" + httpContext.getRequestPath().toLowerCase() + "\n" +
				"date: " + dateOnRFC1123Format + "\n" + "x-content-sha256: " + xContentSHA256 + "\n";

		responseHeaders.add(new HttpString(Headers.X_Digipost_Signature), generateXDigipostSignature(keyPair, signature));

		httpContext.startBlocking();
		IOUtils.copy(new ByteArrayInputStream(bytes), httpContext.getOutputStream());
	}

	private static String generateXContentSHA256(byte[] bytes){
		SHA256Digest digest = new SHA256Digest();
		byte[] entityBytes = bytes;
		digest.update(entityBytes, 0, entityBytes.length);
		byte[] result = new byte[digest.getDigestSize()];
		digest.doFinal(result, 0);

		return new String(Base64.encode(result));
	}

	private static String generateXDigipostSignature(KeyPair keyPair, String signature) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
		instance.initSign(keyPair.getPrivate());
		instance.update(signature.getBytes());
		byte[] signedSignature = instance.sign();
		return new String(Base64.encode(signedSignature));
	}

	public DigipostApiMock start(int port, Map<Method, RequestsAndResponses> requestsAndResponsesMap,
								 KeyPair keyPair) {
		this.keyPair = keyPair;
		this.requestsAndResponsesMap = requestsAndResponsesMap;
		this.port = port;
		this.server = Undertow.builder().addHttpListener(port, "localhost", new BlockingHandler(this)).build();
		server.start();
		LOG.info("Evry SMS service mock running on port {}", port);
		return this;
	}

	public void stop() {
		server.stop();
		LOG.info("Shutting down Evry SMS service mock on port {}", port);
	}

	public void reset() {
		receivedRequests.clear();
	}

	public List<ApiServiceMock.ContentPart> getContentparts(String multipartString){
		List<ApiServiceMock.ContentPart> contentParts = new ArrayList<>();
		String splitString = multipartString.substring(0, multipartString.indexOf("\r\n"));

		String[] split = multipartString.split(splitString);

		for (String bodyPart : split) {
			if(bodyPart.contains("Content-Type: ")) {
				String contentType = bodyPart.substring(bodyPart.indexOf("Content-Type: ") + 14);
				contentType = contentType.substring(0, contentType.indexOf("\r\n"));
				if (!contentType.equals(MediaTypes.DIGIPOST_MEDIA_TYPE_V6)) {
					contentParts.add(new ApiServiceMock.ContentPart(contentType));
				}
			}
		}

		return contentParts;
	}
}
