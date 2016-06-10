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
import no.digipost.api.client.representations.DigipostUri;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Relation;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import static javax.ws.rs.core.HttpHeaders.DATE;

public class DigipostApiMock implements HttpHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DigipostApiMock.class);


	private byte[] stubbedResponse;
	private int port;
	private Undertow server;
	private final BlockingDeque<ReceivedRequest> receivedRequests = new LinkedBlockingDeque<>();
	private Map<Method, RequestsAndResponses> requestsAndResponsesMap;

	@Override
	public void handleRequest(HttpServerExchange httpContext) throws Exception {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		JAXB.marshal(new EntryPoint("Certificate", new Link(Relation.CREATE_MESSAGE, new DigipostUri("http://localhost:9999/"))), bao);
		byte[] bytes = bao.toByteArray();

		HttpString method = httpContext.getRequestMethod();
		httpContext.getRequestHeaders();

		HeaderMap responseHeaders = httpContext.getResponseHeaders();
		String dateOnRFC1123Format = DateUtils.formatDate(DateTime.now());

		responseHeaders.add(new HttpString(DATE), dateOnRFC1123Format);

		String signature = method.toString() + "\n" + httpContext.getRequestPath() + "\n" +
				"date: " + dateOnRFC1123Format + "\n" + "x-digipost-userid: 9999";

		SHA256Digest digest = new SHA256Digest();

		byte[] entityBytes = bytes;
		digest.update(entityBytes, 0, entityBytes.length);
		byte[] result = new byte[digest.getDigestSize()];
		digest.doFinal(result, 0);

		responseHeaders.add(new HttpString(Headers.X_Digipost_Signature), new String(Base64.encode(new byte[]{})));
		responseHeaders.add(new HttpString(Headers.X_Content_SHA256), new String(Base64.encode(result)));
		httpContext.getResponseSender().send(new String(bytes));
	}

	public DigipostApiMock start(int port, Map<Method, RequestsAndResponses> requestsAndResponsesMap) {
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
}
