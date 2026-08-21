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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RequestBearerTokenInterceptorTest {

    @Test
    public void setter_authorization_headeren_med_bearer_prefiks() {
        HttpGet request = new HttpGet("https://api.digipost.no/");

        new RequestBearerTokenInterceptor(() -> "the-token").process(request, null, new BasicHttpContext());

        assertThat(request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue(), is("Bearer the-token"));
    }

    @Test
    public void henter_tokenet_paa_nytt_for_hvert_request() {
        List<String> tokens = new ArrayList<>(List.of("first-token", "second-token"));
        RequestBearerTokenInterceptor interceptor = new RequestBearerTokenInterceptor(() -> tokens.remove(0));

        HttpGet first = new HttpGet("https://api.digipost.no/");
        HttpGet second = new HttpGet("https://api.digipost.no/");
        interceptor.process(first, null, new BasicHttpContext());
        interceptor.process(second, null, new BasicHttpContext());

        assertThat(first.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue(), is("Bearer first-token"));
        assertThat(second.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue(), is("Bearer second-token"));
    }

    @Test
    public void erstatter_en_eksisterende_authorization_header() {
        HttpGet request = new HttpGet("https://api.digipost.no/");
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer stale-token");

        new RequestBearerTokenInterceptor(() -> "fresh-token").process(request, null, new BasicHttpContext());

        assertThat(request.getHeaders(HttpHeaders.AUTHORIZATION).length, is(1));
        assertThat(request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue(), is("Bearer fresh-token"));
    }
}
