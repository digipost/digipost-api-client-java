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
package no.digipost.api.client.filters.request;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.Headers;
import no.digipost.api.client.util.DateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;
import static no.digipost.api.client.Headers.Date;

@Priority(Priorities.HEADER_DECORATOR)
public class RequestDateFilter implements ClientRequestFilter {
	private static final Logger LOG = LoggerFactory.getLogger(RequestDateFilter.class);
	private final EventLogger eventLogger;

	public RequestDateFilter(final EventLogger eventListener) {
		this.eventLogger = eventListener != null ? eventListener : NOOP_EVENT_LOGGER;
	}

	@Override
	public void filter(ClientRequestContext clientRequestContext) throws IOException {
		modifyRequest(clientRequestContext);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	private ClientRequestContext modifyRequest(final ClientRequestContext cr) {
		String dateOnRFC1123Format = DateUtils.formatDate(DateTime.now());
		cr.getHeaders().add(Date, dateOnRFC1123Format);
		log(getClass().getSimpleName() + " satt headeren " + Headers.Date + "=" + dateOnRFC1123Format);
		return cr;
	}


}
