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
package no.digipost.api.client.internal.http;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecRuntime;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.digipost.api.client.internal.http.request.interceptor.RequestBearerTokenInterceptor.ATTEMPTED_ACCESS_TOKEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class RefreshAccessTokenOnUnauthorizedExecTest {

    private final List<String> invalidatedTokens = new ArrayList<>();
    private final RefreshAccessTokenOnUnauthorizedExec exec = new RefreshAccessTokenOnUnauthorizedExec(invalidatedTokens::add);

    @Test
    void sender_requesten_paa_nytt_naar_tokenet_blir_avvist() throws Exception {
        RespondingChain chain = new RespondingChain(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_OK);

        ClassicHttpResponse response = exec.execute(get(), scopeWithAttemptedToken("rejected-token"), chain);

        assertThat(response.getCode(), is(HttpStatus.SC_OK));
        assertThat(chain.receivedRequestCount(), is(2));
        assertThat(invalidatedTokens, contains("rejected-token"));
    }

    @Test
    void sender_requesten_paa_nytt_bare_en_gang() throws Exception {
        RespondingChain chain = new RespondingChain(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_UNAUTHORIZED);

        ClassicHttpResponse response = exec.execute(get(), scopeWithAttemptedToken("rejected-token"), chain);

        assertThat(response.getCode(), is(HttpStatus.SC_UNAUTHORIZED));
        assertThat(chain.receivedRequestCount(), is(2));
    }

    @Test
    void roerer_ikke_svar_som_ikke_er_401() throws Exception {
        RespondingChain chain = new RespondingChain(HttpStatus.SC_FORBIDDEN, HttpStatus.SC_OK);

        ClassicHttpResponse response = exec.execute(get(), scopeWithAttemptedToken("the-token"), chain);

        assertThat(response.getCode(), is(HttpStatus.SC_FORBIDDEN));
        assertThat(chain.receivedRequestCount(), is(1));
        assertThat(invalidatedTokens, is(empty()));
    }

    @Test
    void gjoer_ingenting_naar_requesten_ikke_ble_sendt_med_et_token() throws Exception {
        RespondingChain chain = new RespondingChain(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_OK);

        ClassicHttpResponse response = exec.execute(get(), scope(HttpClientContext.create()), chain);

        assertThat(response.getCode(), is(HttpStatus.SC_UNAUTHORIZED));
        assertThat(chain.receivedRequestCount(), is(1));
        assertThat(invalidatedTokens, is(empty()));
    }

    @Test
    void sender_ikke_innhold_som_ikke_kan_sendes_paa_nytt() throws Exception {
        RespondingChain chain = new RespondingChain(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_OK);
        ClassicHttpRequest post = post(new InputStreamEntity(new ByteArrayInputStream("content".getBytes(UTF_8)), 7, null));

        ClassicHttpResponse response = exec.execute(post, scopeWithAttemptedToken("rejected-token"), chain);

        assertThat(response.getCode(), is(HttpStatus.SC_UNAUTHORIZED));
        assertThat(chain.receivedRequestCount(), is(1));
        assertThat("tokenet skal ikke kastes når vi likevel ikke kan prøve på nytt", invalidatedTokens, is(empty()));
    }

    @Test
    void sender_innhold_som_kan_sendes_paa_nytt() throws Exception {
        RespondingChain chain = new RespondingChain(HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_OK);

        ClassicHttpResponse response = exec.execute(post(new StringEntity("content", UTF_8)), scopeWithAttemptedToken("rejected-token"), chain);

        assertThat(response.getCode(), is(HttpStatus.SC_OK));
        assertThat(chain.receivedRequestCount(), is(2));
    }

    private static ClassicHttpRequest get() {
        return new HttpGet("https://api.digipost.no/");
    }

    private static ClassicHttpRequest post(HttpEntity entity) {
        HttpPost post = new HttpPost("https://api.digipost.no/");
        post.setEntity(entity);
        return post;
    }

    private static ExecChain.Scope scopeWithAttemptedToken(String token) {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(ATTEMPTED_ACCESS_TOKEN, token);
        return scope(context);
    }

    private static ExecChain.Scope scope(HttpClientContext context) {
        return new ExecChain.Scope("test-exchange", new HttpRoute(new HttpHost("https", "api.digipost.no", 443)), get(), mock(ExecRuntime.class), context);
    }


    /**
     * Answers each request with the next of the given statuses, keeping the last one once they
     * are exhausted, and records the requests it was asked to send.
     */
    private static final class RespondingChain implements ExecChain {

        private final List<Integer> statuses;
        private final List<ClassicHttpRequest> receivedRequests = new ArrayList<>();

        RespondingChain(Integer... statuses) {
            this.statuses = List.of(statuses);
        }

        @Override
        public ClassicHttpResponse proceed(ClassicHttpRequest request, Scope scope) {
            int status = statuses.get(Math.min(receivedRequests.size(), statuses.size() - 1));
            receivedRequests.add(request);
            return new BasicClassicHttpResponse(status);
        }

        int receivedRequestCount() {
            return receivedRequests.size();
        }
    }
}
