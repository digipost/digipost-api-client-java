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

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.ErrorType;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.Headers;
import no.digipost.api.client.security.ClientResponseToVerify;
import no.digipost.api.client.security.ResponseMessageSignatureUtil;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ResponseSignatureFilter extends ClientFilter {

	private final EventLogger eventLogger;

	public ResponseSignatureFilter() {
		this(NOOP_EVENT_LOGGER);
	}

	public ResponseSignatureFilter(final EventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	@Override
	public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
		ClientResponse response = getNext().handle(cr);

		String serverSignaturBase64 = getServerSignaturFromResponse(response);
		byte[] serverSignaturBytes = Base64.decodeBase64(serverSignaturBase64.getBytes());

		String signatureString = ResponseMessageSignatureUtil.getCanonicalResponseRepresentation(new ClientResponseToVerify(cr, response));

		try {
			Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
			instance.initVerify(lastSertifikat());
			instance.update(signatureString.getBytes());
			boolean verified = instance.verify(serverSignaturBytes);
			if (!verified) {
				throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR, "Melding fra server matcher ikke signatur.");
			} else {
				eventLogger.log("Verifiserte signert respons fra Digipost. Signatur fra HTTP-headeren " + Headers.X_Digipost_Signature
						+ " var OK: " + new String(serverSignaturBase64));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR, "Det skjedde en feil under signatursjekk.");
		}

		return response;
	}

	private String getServerSignaturFromResponse(final ClientResponse response) {
		String serverSignaturString = response.getHeaders().getFirst(Headers.X_Digipost_Signature);
		if (StringUtils.isBlank(serverSignaturString)) {
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
					"Mangler signatur-header, så server-signatur kunne ikke sjekkes");
		}
		return serverSignaturString;
	}

	public X509Certificate lastSertifikat() {
		try {
			InputStream certStream = getClass().getResourceAsStream("/keys/digipost-utv.pem");

			CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
			X509Certificate sertifikat = (X509Certificate) cf.generateCertificate(certStream);
			if (sertifikat == null) {
				throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
						"Kunne ikke laste Digipost's public key, så server-signatur kunne ikke sjekkes");
			}
			return sertifikat;
		} catch (GeneralSecurityException e) {
			throw new DigipostClientException(ErrorType.SERVER_SIGNATURE_ERROR,
					"Kunne ikke laste Digipost's public key, så server-signatur kunne ikke sjekkes");
		}
	}

}
