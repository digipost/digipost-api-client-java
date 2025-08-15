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
import org.apache.hc.core5.http.HttpRequest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestContentHashFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RequestContentHashFilter.class);

    private final EventLogger eventLogger;
    private final Digester digester;
    private final String header;

    public RequestContentHashFilter(EventLogger eventLogger, Digester digester, String header) {
        this.eventLogger = (eventLogger != null ? eventLogger : EventLogger.NOOP_LOGGER).withDebugLogTo(LOG);
        this.digester = digester;
        this.header = header;
    }

    public RequestContentHashFilter(Digester digester, final String header) {
        this(EventLogger.NOOP_LOGGER, digester, header);
    }

    public void settContentHashHeader(final byte[] data, final HttpRequest httpRequest) {
        byte[] result = digester.createDigest(data);
        String hash = new String(Base64.encode(result));
        httpRequest.setHeader(header, hash);
        eventLogger.log(RequestContentHashFilter.class.getSimpleName() + " satt headeren " + header + "=" + hash);
    }
}
