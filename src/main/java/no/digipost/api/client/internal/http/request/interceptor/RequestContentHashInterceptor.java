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

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.security.Digester;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class RequestContentHashInterceptor implements HttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestContentHashInterceptor.class);

    private final EventLogger eventLogger;
    private final Digester digester;
    private final String header;

    public RequestContentHashInterceptor(Digester digester, String header) {
        this(EventLogger.NOOP_LOGGER, digester, header);
    }

    public RequestContentHashInterceptor(EventLogger eventLogger, Digester digester, String header) {
        this.eventLogger = (eventLogger != null ? eventLogger : EventLogger.NOOP_LOGGER).withDebugLogTo(LOG);
        this.digester = digester;
        this.header = header;
    }

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) throws IOException {
        if (!(httpRequest instanceof ClassicHttpRequest)) {
            return;
        }
        HttpEntity entity = ((ClassicHttpRequest) httpRequest).getEntity();
        if (entity == null) {
            return;
        }
        byte[] data = Optional.ofNullable(EntityUtils.toByteArray(entity)).orElseGet(() -> new byte[0]);
        String hash = new String(Base64.encode(digester.createDigest(data)));
        httpRequest.setHeader(header, hash);
        eventLogger.log(getClass().getSimpleName() + " satt headeren " + header + "=" + hash);
    }
}
