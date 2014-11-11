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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.DATE;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.joda.time.DateTime.now;

public class ResponseDateFilter implements ClientResponseFilter {
	private static final Logger LOG = LoggerFactory.getLogger(ResponseDateFilter.class);
	private static final int AKSEPTABEL_TIDSDIFFERANSE_MINUTTER = 5;

	private boolean shouldThrow = true;

	public void setThrowOnError(final boolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}

	@Override
	public void filter(final ClientRequestContext clientRequestContext, final ClientResponseContext clientResponseContext) throws IOException {
		String dateHeader = clientResponseContext.getHeaders().getFirst(DATE);
		try {
			if (isNotBlank(dateHeader)) {
				sjekkDato(dateHeader);
			} else {
				throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
						"Mangler Date-header - server-signatur kunne ikke sjekkes");
			}
		} catch (Exception e) {
			if (shouldThrow) {
				if (e instanceof DigipostClientException) {
					throw (DigipostClientException) e;
				} else {
					throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
							"Det skjedde en feil under signatursjekk: " + e.getMessage());
				}
			} else {
				LOG.warn("Feil under validering av server-signatur", e);
			}
		}
	}


	private void sjekkDato(final String dateOnRFC1123Format) {
		try {
			DateTime date = DateUtils.parseDate(dateOnRFC1123Format);
			sjekkAtDatoHeaderIkkeErForGammel(dateOnRFC1123Format, date);
			sjekkAtDatoHeaderIkkeErForNy(dateOnRFC1123Format, date);
		} catch (IllegalArgumentException e) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
					"Date-header kunne ikke parses - server-signatur kunne ikke sjekkes");

		}
	}

	private void sjekkAtDatoHeaderIkkeErForGammel(final String headerDate, final DateTime parsedDate) {
		if (parsedDate.isBefore(now().minusMinutes(AKSEPTABEL_TIDSDIFFERANSE_MINUTTER))) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Date-header fra server er for gammel: " + headerDate);
		}
	}

	private void sjekkAtDatoHeaderIkkeErForNy(final String headerDate, final DateTime parsedDate) {
		if (parsedDate.isAfter(now().plusMinutes(AKSEPTABEL_TIDSDIFFERANSE_MINUTTER))) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Date-header fra server er for ny: " + headerDate);
		}
	}

}
