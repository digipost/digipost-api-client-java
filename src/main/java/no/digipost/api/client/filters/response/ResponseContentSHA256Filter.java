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
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static no.digipost.api.client.Headers.X_Content_SHA256;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class ResponseContentSHA256Filter implements ClientResponseFilter {
	private static final Logger LOG = LoggerFactory.getLogger(ResponseContentSHA256Filter.class);

	private boolean shouldThrow = true;

	public void setThrowOnError(final boolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}


	@Override
	public void filter(final ClientRequestContext clientRequestContext, final ClientResponseContext clientResponseContext) throws IOException {
		if (clientResponseContext.hasEntity()) {
			try {
				validerContentHash(clientResponseContext);
			} catch (Exception e) {
				logOrThrow("Det skjedde en feil under signatursjekk: " + e.getMessage(), e);
			}
		}
	}

	private void validerContentHash(final ClientResponseContext response) {
		try {
			String hashHeader = response.getHeaders().getFirst(X_Content_SHA256);
			if (isBlank(hashHeader)) {
				throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
						"Ikke definert X-Content-SHA256-header, så server-signatur kunne ikke sjekkes");
			}
			byte[] entityBytes = IOUtils.toByteArray(response.getEntityStream());
			validerBytesMotHashHeader(hashHeader, entityBytes);
			response.setEntityStream(new ByteArrayInputStream(entityBytes));
		} catch (IOException e) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
					"Det skjedde en feil under uthenting av innhold for validering av X-Content-SHA256-header, så server-signatur kunne ikke sjekkes");
		}
	}

	private void validerBytesMotHashHeader(final String serverHash, final byte[] entityBytes) {
		SHA256Digest digest = new SHA256Digest();

		digest.update(entityBytes, 0, entityBytes.length);
		byte[] result = new byte[digest.getDigestSize()];
		digest.doFinal(result, 0);
		String ourHash = new String(Base64.encode(result));
		if (!serverHash.equals(ourHash)) {
			logOrThrow("X-Content-SHA256-header matchet ikke innholdet, så server-signatur er feil.", null);
		}
	}

	private void logOrThrow(final String message, final Exception e) {
		if (shouldThrow) {
			if (e instanceof DigipostClientException) {
				throw (DigipostClientException) e;
			} else {
				throw new DigipostClientException(SERVER_SIGNATURE_ERROR, message);
			}
		} else {
			LOG.warn("Feil under validering av server signatur", e);
		}
	}
}
