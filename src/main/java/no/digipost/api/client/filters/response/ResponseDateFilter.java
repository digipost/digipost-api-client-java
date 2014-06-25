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

import no.digipost.api.client.Headers;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.DateUtils;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ResponseDateFilter implements ClientResponseFilter {

	private static final int AKSEPTABEL_TIDSDIFFERANSE_MINUTTER = 5;

	@Override
	public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
		String dateHeader = clientResponseContext.getHeaders().getFirst(Headers.Date);

		if(!StringUtils.isBlank(dateHeader)) {
			sjekkDato(dateHeader);
		} else {
			throw new DigipostClientException(ErrorCode.SERVER_SIGNATURE_ERROR,
					"Mangler dato-header, så server-signatur kunne ikke sjekkes");
		}
	}


	private void sjekkDato(final String dateOnRFC1123Format) {
		Date date = DateUtils.parseDate(dateOnRFC1123Format);
		if (date == null) {
			throw new DigipostClientException(ErrorCode.SERVER_SIGNATURE_ERROR,
					"Dato-header kunne ikke parses, så server-signatur kunne ikke sjekkes");
		}
		sjekkAtDatoHeaderIkkeErForGammel(dateOnRFC1123Format, date);
		sjekkAtDatoHeaderIkkeErForNy(dateOnRFC1123Format, date);

	}

	private void sjekkAtDatoHeaderIkkeErForGammel(final String headerDate, final Date parsedDate) {
		int akseptabelTidsdifferanse = AKSEPTABEL_TIDSDIFFERANSE_MINUTTER;
		Calendar tidligsteTillatteTidspunkt = Calendar.getInstance();
		tidligsteTillatteTidspunkt.add(Calendar.MINUTE, -akseptabelTidsdifferanse);
		if (parsedDate.before(tidligsteTillatteTidspunkt.getTime())) {
			throw new DigipostClientException(ErrorCode.SERVER_SIGNATURE_ERROR, "Dato-header fra server er for gammel: " + headerDate);
		}
	}

	private void sjekkAtDatoHeaderIkkeErForNy(final String headerDate, final Date parsedDate) {
		int akseptabelTidsdifferanse = AKSEPTABEL_TIDSDIFFERANSE_MINUTTER;
		Calendar senesteTillatteTidspunkt = Calendar.getInstance();
		senesteTillatteTidspunkt.add(Calendar.MINUTE, akseptabelTidsdifferanse);
		if (parsedDate.after(senesteTillatteTidspunkt.getTime())) {
			throw new DigipostClientException(ErrorCode.SERVER_SIGNATURE_ERROR, "Dato-header fra server er for ny: " + headerDate);
		}
	}
}
