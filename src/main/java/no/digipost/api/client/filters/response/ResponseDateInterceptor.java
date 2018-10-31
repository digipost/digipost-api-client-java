/**
 * Copyright (C) Posten Norge AS
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
package no.digipost.api.client.filters.response;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.time.ZonedDateTime.now;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.http.HttpHeaders.DATE;

public class ResponseDateInterceptor implements HttpResponseInterceptor {
    private static final Duration ACCEPTABLE_TIME_DIFF = Duration.ofMinutes(5);

    private final Clock clock;

    public ResponseDateInterceptor() {
        this(Clock.systemDefaultZone());
    }

    public ResponseDateInterceptor(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        final String dateHeader = Optional.ofNullable(response.getFirstHeader(DATE))
                .map(h -> h.getValue())
                .filter(StringUtils::isNoneBlank)
                .orElseThrow(() -> new DigipostClientException(SERVER_SIGNATURE_ERROR,
                    String.format("Missing %s header in response. This header is expected in all response. Http status was %s",
                            DATE, response.getStatusLine())));
        checkDate(dateHeader);
    }

    private void checkDate(String dateOnRFC1123Format) {
        try {
            ZonedDateTime date = DateUtils.parseDate(dateOnRFC1123Format);
            sjekkAtDatoHeaderIkkeErForGammel(dateOnRFC1123Format, date);
            sjekkAtDatoHeaderIkkeErForNy(dateOnRFC1123Format, date);
        } catch (DateTimeParseException e) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Unable to parse Date header '" + dateOnRFC1123Format + "' (is it 'RFC 1123 compliant'?)");
        }
    }

    private void sjekkAtDatoHeaderIkkeErForGammel(final String headerDate, final ZonedDateTime parsedDate) {
        if (parsedDate.isBefore(now(clock).minus(ACCEPTABLE_TIME_DIFF))) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Date header in response from server is too old: " + headerDate);
        }
    }

    private void sjekkAtDatoHeaderIkkeErForNy(final String headerDate, final ZonedDateTime parsedDate) {
        if (parsedDate.isAfter(now(clock).plus(ACCEPTABLE_TIME_DIFF))) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Date-header from server is too early: " + headerDate);
        }
    }
}
