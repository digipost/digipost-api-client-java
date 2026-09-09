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
import no.digipost.api.client.security.Signer;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class RequestSignatureInterceptorTest {

    private final AtomicReference<String> signedContent = new AtomicReference<>();
    private final Signer capturingSigner = dataToSign -> {
        signedContent.set(dataToSign);
        return new byte[0];
    };

    private final RequestContentHashInterceptor contentHashInterceptor =
            new RequestContentHashInterceptor(Digester.sha256, Headers.X_Content_SHA256);
    private final RequestSignatureInterceptor signatureInterceptor = new RequestSignatureInterceptor(capturingSigner);

    @Test
    public void signerer_over_innholdshashen_naar_interceptorene_kjoerer_i_registrert_rekkefoelge() throws IOException, NoSuchAlgorithmException {
        byte[] body = "digipost".getBytes(StandardCharsets.UTF_8);
        HttpPost request = new HttpPost("https://api.digipost.no/api/documents");
        request.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM));

        contentHashInterceptor.process(request, null, new BasicHttpContext());
        signatureInterceptor.process(request, null, new BasicHttpContext());

        String expectedHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(body));
        assertThat(signedContent.get(), containsString(Headers.X_Content_SHA256.toLowerCase() + ": " + expectedHash));
        assertThat(request.getFirstHeader(Headers.X_Digipost_Signature), notNullValue());
    }

    @Test
    public void signerer_request_uten_innhold() {
        HttpGet request = new HttpGet("https://api.digipost.no/api/documents");

        assertDoesNotThrow(() -> signatureInterceptor.process(request, null, new BasicHttpContext()));

        assertThat(request.getFirstHeader(Headers.X_Digipost_Signature), notNullValue());
    }
}
