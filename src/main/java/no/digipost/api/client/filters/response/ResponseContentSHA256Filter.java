package no.digipost.api.client.filters.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.ErrorType;
import no.digipost.api.client.Headers;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ResponseContentSHA256Filter extends ClientFilter {

	@Override
	public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
		ClientResponse response = getNext().handle(cr);

		if (response.hasEntity()) {
			validerContentHash(response);
		}
		return response;
	}

	private void validerContentHash(final ClientResponse response) {
		try {
			String hashHeader = response.getHeaders().getFirst(Headers.X_Content_SHA256);
			if (StringUtils.isBlank(hashHeader)) {
				throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
						"Ikke definert X-Content-SHA256-header, så server-signatur kunne ikke sjekkes");
			}
			byte[] entityBytes = IOUtils.toByteArray(response.getEntityInputStream());
			validerBytesMotHashHeader(hashHeader, entityBytes);
			response.setEntityInputStream(new ByteArrayInputStream(entityBytes));
		} catch (IOException e) {
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
					"Det skjedde en feil under uthenting av innhold for validering av X-Content-SHA256-header, så server-signatur kunne ikke sjekkes");
		}
	}

	private void validerBytesMotHashHeader(final String clientHash, final byte[] entityBytes) {
		SHA256Digest digest = new SHA256Digest();

		digest.update(entityBytes, 0, entityBytes.length);
		byte[] result = new byte[digest.getDigestSize()];
		digest.doFinal(result, 0);
		String serverHash = new String(Base64.encodeBase64(result));
		if (!clientHash.equals(serverHash)) {
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
					"X-Content-SHA256-header matchet ikke innholdet, så server-signatur er feil.");
		}
	}

}
