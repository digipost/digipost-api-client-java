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

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public final class VerifyUnlessUnauthorized implements HttpResponseInterceptor {

    public static HttpResponseInterceptor unlessUnauthorized(HttpResponseInterceptor verification) {
        return new VerifyUnlessUnauthorized(verification);
    }

    private final HttpResponseInterceptor verification;

    private VerifyUnlessUnauthorized(HttpResponseInterceptor verification) {
        this.verification = requireNonNull(verification, "verification cannot be null");
    }

    @Override
    public void process(HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
        if (response.getCode() == HttpStatus.SC_UNAUTHORIZED) {
            return;
        }
        verification.process(response, entityDetails, context);
    }
}
