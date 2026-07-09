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
package no.digipost.api.client.internal.http.request.interceptor;

import no.digipost.api.client.internal.http.Headers;
import no.digipost.api.client.security.Digester;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class RequestContentHashInterceptorTest {

    private final RequestContentHashInterceptor interceptor =
            new RequestContentHashInterceptor(Digester.sha256, Headers.X_Content_SHA256);

    @Test
    public void setter_sha256_header_beregnet_over_request_body() throws IOException, NoSuchAlgorithmException {
        byte[] body = "digipost".getBytes(StandardCharsets.UTF_8);
        HttpPost request = new HttpPost("https://api.digipost.no/");
        request.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM));

        interceptor.process(request, null, new BasicHttpContext());

        String expected = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(body));
        assertThat(request.getFirstHeader(Headers.X_Content_SHA256), notNullValue());
        assertThat(request.getFirstHeader(Headers.X_Content_SHA256).getValue(), is(expected));
    }

    @Test
    public void setter_hash_over_tom_body() throws IOException, NoSuchAlgorithmException {
        HttpPost request = new HttpPost("https://api.digipost.no/");
        request.setEntity(new ByteArrayEntity(new byte[0], ContentType.APPLICATION_OCTET_STREAM));

        interceptor.process(request, null, new BasicHttpContext());

        String expected = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(new byte[0]));
        assertThat(request.getFirstHeader(Headers.X_Content_SHA256).getValue(), is(expected));
    }

    @Test
    public void setter_ingen_header_naar_request_ikke_har_body() throws IOException {
        HttpGet request = new HttpGet("https://api.digipost.no/");

        interceptor.process(request, null, new BasicHttpContext());

        assertThat(request.getFirstHeader(Headers.X_Content_SHA256), nullValue());
    }
}
