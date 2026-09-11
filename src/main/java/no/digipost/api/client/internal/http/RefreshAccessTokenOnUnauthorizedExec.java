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

import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.client5.http.classic.ExecChainHandler;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static no.digipost.api.client.internal.http.request.interceptor.RequestBearerTokenInterceptor.ATTEMPTED_ACCESS_TOKEN;

public class RefreshAccessTokenOnUnauthorizedExec implements ExecChainHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RefreshAccessTokenOnUnauthorizedExec.class);

    private final Consumer<String> invalidateAccessToken;

    public RefreshAccessTokenOnUnauthorizedExec(Consumer<String> invalidateAccessToken) {
        this.invalidateAccessToken = requireNonNull(invalidateAccessToken, "invalidateAccessToken cannot be null");
    }

    @Override
    public ClassicHttpResponse execute(ClassicHttpRequest request, ExecChain.Scope scope, ExecChain chain) throws IOException, HttpException {
        ClassicHttpResponse response = chain.proceed(request, scope);
        if (response.getCode() != HttpStatus.SC_UNAUTHORIZED) {
            return response;
        }

        Object rejectedToken = scope.clientContext.getAttribute(ATTEMPTED_ACCESS_TOKEN);
        if (!(rejectedToken instanceof String)) return response;

        HttpEntity entity = request.getEntity();
        if (entity != null && !entity.isRepeatable()) {
            LOG.info("{} was rejected with 401, but cannot be re-sent with a new access token as its content is not repeatable", scope.exchangeId);
            return response;
        }

        invalidateAccessToken.accept((String) rejectedToken);
        response.close();

        LOG.info("{} was rejected with 401; re-sending it once with a newly fetched access token", scope.exchangeId);
        return chain.proceed(ClassicRequestBuilder.copy(request).build(), scope);
    }
}
