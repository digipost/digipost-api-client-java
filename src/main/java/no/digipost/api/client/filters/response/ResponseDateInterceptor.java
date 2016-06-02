package no.digipost.api.client.filters.response;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.util.DateUtils;
import no.digipost.api.client.util.LoggingUtil;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static javax.ws.rs.core.HttpHeaders.DATE;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.joda.time.DateTime.now;

public class ResponseDateInterceptor implements HttpResponseInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseDateFilter.class);
	private static final int AKSEPTABEL_TIDSDIFFERANSE_MINUTTER = 5;

	private boolean shouldThrow = true;

	public void setThrowOnError(final boolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		String dateHeader = "";
		for(Header header : response.getAllHeaders()){
			if(header.getName().equals(DATE)){
				dateHeader = header.getValue();
				break;
			}
		}

		try {
			if (isNotBlank(dateHeader)) {
				sjekkDato(dateHeader);
			} else {
				throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
						"Mangler Date-header - server-signatur kunne ikke sjekkes");
			}
		} catch (Exception e) {
			LoggingUtil.logResponse(response);
			if (shouldThrow) {
				if (e instanceof DigipostClientException) {
					throw (DigipostClientException) e;
				} else {
					throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
							"Det skjedde en feil under signatursjekk: " + e.getMessage());
				}
			} else {
				LOG.warn("Feil under validering av server-signatur: '" + e.getMessage() + "'. " +
						(LOG.isDebugEnabled() ? "" : "Konfigurer debug-logging for " + LOG.getName() + " for å se full stacktrace."));
				LOG.debug(e.getMessage(), e);
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
