/*
 * Copyright (C) Posten Bring AS
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

import com.sun.net.httpserver.HttpServer;
import org.apache.hc.core5.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static no.digipost.api.client.internal.http.Headers.X_Content_SHA256;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;

/**
 * A local HTTP server standing in for the Digipost API, answering each request with the next of
 * the responses it is stubbed with, and recording the {@code Authorization} header of every
 * request it received.
 * <p>
 * It speaks plain HTTP: the JWT client only presents its client certificate towards the token
 * endpoint, and its TLS configuration is of no consequence to the requests tested here.
 */
final class DigipostApiStub implements Closeable {

    private final HttpServer server;
    private final URI uri;

    private final List<String> receivedAuthorizationHeaders = new ArrayList<>();
    private volatile List<StubbedResponse> responses = List.of();

    DigipostApiStub() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            StubbedResponse response;
            synchronized (receivedAuthorizationHeaders) {
                receivedAuthorizationHeaders.add(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
                List<StubbedResponse> stubbed = responses;
                response = stubbed.get(Math.min(receivedAuthorizationHeaders.size() - 1, stubbed.size() - 1));
            }
            exchange.getRequestBody().readAllBytes();

            if (response.digipostHeaders) {
                // The Digipost API dates and hashes its responses, and the client rejects responses lacking it.
                exchange.getResponseHeaders().set(HttpHeaders.DATE, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC)));
                exchange.getResponseHeaders().set(X_Content_SHA256, sha256Base64(response.body));
            }
            exchange.sendResponseHeaders(response.status, response.body.length == 0 ? -1 : response.body.length);
            exchange.getResponseBody().write(response.body);
            exchange.close();
        });
        server.start();

        this.uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort());
    }

    URI uri() {
        return uri;
    }

    /** Answer the requests with these responses in order, repeating the last one if more requests arrive. */
    void respondWith(StubbedResponse... responses) {
        this.responses = List.of(responses);
    }

    List<String> receivedAuthorizationHeaders() {
        synchronized (receivedAuthorizationHeaders) {
            return unmodifiableList(new ArrayList<>(receivedAuthorizationHeaders));
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }

    /** A response from the Digipost application itself, carrying the headers it dates and hashes its responses with. */
    static StubbedResponse marshalled(int status, Object representation) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        marshal(jaxbContext, representation, body);
        return new StubbedResponse(status, body.toByteArray(), true);
    }

    /**
     * A response carrying none of the headers the Digipost application would have added. This is
     * what a request rejected before it reaches the application, e.g. by a gateway refusing its
     * access token, looks like.
     */
    static StubbedResponse withoutDigipostHeaders(int status, String body) {
        return new StubbedResponse(status, body.getBytes(UTF_8), false);
    }

    static final class StubbedResponse {
        final int status;
        final byte[] body;
        final boolean digipostHeaders;

        private StubbedResponse(int status, byte[] body, boolean digipostHeaders) {
            this.status = status;
            this.body = body;
            this.digipostHeaders = digipostHeaders;
        }
    }

    private static String sha256Base64(byte[] content) {
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
