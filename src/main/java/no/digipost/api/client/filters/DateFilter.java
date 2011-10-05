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
package no.digipost.api.client.filters;

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;
import static no.digipost.api.client.Headers.Date;

import java.util.Date;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.Headers;

import org.apache.commons.httpclient.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class DateFilter extends ClientFilter {
	private static final Logger LOG = LoggerFactory.getLogger(DateFilter.class);
	private final EventLogger eventLogger;

	public DateFilter() {
		this(NOOP_EVENT_LOGGER);
	}

	public DateFilter(final EventLogger eventListener) {
		this.eventLogger = eventListener != null ? eventListener : NOOP_EVENT_LOGGER;
	}

	@Override
	public ClientResponse handle(final ClientRequest cr) {
		modifyRequest(cr);
		return getNext().handle(cr);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	private ClientRequest modifyRequest(final ClientRequest cr) {
		String dateOnRFC1123Format = DateUtil.formatDate(new Date());
		cr.getHeaders().add(Date, dateOnRFC1123Format);
		log(getClass().getSimpleName() + " satt headeren " + Headers.Date + "=" + dateOnRFC1123Format);
		return cr;
	}

}
