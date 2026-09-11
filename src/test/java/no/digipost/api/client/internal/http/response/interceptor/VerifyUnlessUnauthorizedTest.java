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
package no.digipost.api.client.internal.http.response.interceptor;

import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static no.digipost.api.client.internal.http.response.interceptor.VerifyUnlessUnauthorized.unlessUnauthorized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class VerifyUnlessUnauthorizedTest {

    private final List<Integer> verifiedResponses = new ArrayList<>();
    private final HttpResponseInterceptor verification =
            unlessUnauthorized((response, entityDetails, context) -> verifiedResponses.add(response.getCode()));

    @Test
    void verifiserer_vanlige_svar() throws Exception {
        verification.process(new BasicClassicHttpResponse(HttpStatus.SC_OK), null, new BasicHttpContext());
        verification.process(new BasicClassicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR), null, new BasicHttpContext());

        assertThat(verifiedResponses, contains(HttpStatus.SC_OK, HttpStatus.SC_INTERNAL_SERVER_ERROR));
    }

    @Test
    void verifiserer_ikke_svar_om_at_tokenet_ble_avvist() throws Exception {
        verification.process(new BasicClassicHttpResponse(HttpStatus.SC_UNAUTHORIZED), null, new BasicHttpContext());

        assertThat(verifiedResponses, is(empty()));
    }
}
