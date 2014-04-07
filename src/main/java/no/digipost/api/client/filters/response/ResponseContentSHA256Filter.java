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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.ErrorType;
import no.digipost.api.client.Headers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;


import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;


public class ResponseContentSHA256Filter implements ClientResponseFilter {

	@Override
	public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
		if (clientResponseContext.hasEntity()) {
			validerContentHash(clientResponseContext);
		}
	}

	private void validerContentHash(final ClientResponseContext response) {
		try {
			String hashHeader = response.getHeaders().getFirst(Headers.X_Content_SHA256);
			if (StringUtils.isBlank(hashHeader)) {
				throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
						"Ikke definert X-Content-SHA256-header, så server-signatur kunne ikke sjekkes");
			}
			byte[] entityBytes = IOUtils.toByteArray(response.getEntityStream());
			validerBytesMotHashHeader(hashHeader, entityBytes);
			response.setEntityStream(new ByteArrayInputStream(entityBytes));
		} catch (IOException e) {
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
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
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
					"X-Content-SHA256-header matchet ikke innholdet, så server-signatur er feil.");
		}
	}
}
