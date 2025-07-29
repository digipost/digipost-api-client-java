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
import no.digipost.api.client.util.DateUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.apache.http.HttpHeaders.DATE;

public class RequestDateInterceptor implements HttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestDateInterceptor.class);
    private final EventLogger eventLogger;
    private final Clock clock;

    public RequestDateInterceptor(EventLogger eventLogger, Clock clock) {
        this.eventLogger = (eventLogger != null ? eventLogger : EventLogger.NOOP_LOGGER).withDebugLogTo(LOG);
        this.clock = clock;
    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) {
        modifyRequest(httpRequest);
    }

    private void modifyRequest(final HttpRequest httpRequest) {
        String dateOnRFC1123Format = DateUtils.formatDate(ZonedDateTime.now(clock));
        httpRequest.setHeader(DATE, dateOnRFC1123Format);
        eventLogger.log(getClass().getSimpleName() + " satt headeren " + DATE + "=" + dateOnRFC1123Format);
    }
}
