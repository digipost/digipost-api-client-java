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

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.security.ClientResponseToVerify;
import no.digipost.api.client.security.ResponseMessageSignatureUtil;
import no.digipost.api.client.util.Supplier;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;
import static no.digipost.api.client.Headers.X_Digipost_Signature;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseSignatureInterceptor implements HttpResponseInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseSignatureInterceptor.class);
	private boolean shouldThrow = true;

	public void setThrowOnError(final boolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}

	private final EventLogger eventLogger;
	private final Supplier<byte[]> certificateSupplier;

	public ResponseSignatureInterceptor(final Supplier<byte[]> certificateSupplier) {
		this(NOOP_EVENT_LOGGER, certificateSupplier);
	}

	public ResponseSignatureInterceptor(final EventLogger eventLogger, final Supplier<byte[]> certificateSupplier) {
		this.eventLogger = eventLogger;
		this.certificateSupplier = certificateSupplier;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		if ("/".equals(((CookieOrigin)(context.getAttribute("http.cookie-origin"))).getPath())) {
			eventLogger.log("Verifiserer ikke signatur fordi det er rotressurs vi hentet.");
			return;
		}

		try {
			String serverSignaturBase64 = getServerSignaturFromResponse(response);
			byte[] serverSignaturBytes = Base64.decode(serverSignaturBase64.getBytes());

			String signatureString = ResponseMessageSignatureUtil.getCanonicalResponseRepresentation(new ClientResponseToVerify(context, response));

			Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
			instance.initVerify(lastSertifikat());
			instance.update(signatureString.getBytes());
			boolean verified = instance.verify(serverSignaturBytes);
			if (!verified) {
				throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Melding fra server matcher ikke signatur.");
			} else {
				eventLogger.log("Verifiserte signert respons fra Digipost. Signatur fra HTTP-headeren " + X_Digipost_Signature
						+ " var OK: " + serverSignaturBase64);
			}
		} catch (Exception e) {
			if (shouldThrow) {
				unwrapAndThrowException(e);
			} else {
				LOG.warn("Feil under validering av server signatur: '" + e.getMessage() + "'. " +
						(LOG.isDebugEnabled() ? "" : "Konfigurer debug-logging for " + LOG.getName() + " for å se full stacktrace."));
				LOG.debug(e.getMessage(), e);
			}
		}
	}

	static void unwrapAndThrowException(final Exception e) {
		if (e instanceof DigipostClientException) {
			throw (DigipostClientException) e;
		} else if (e.getCause() instanceof DigipostClientException) {
			throw (DigipostClientException) e.getCause();
		} else {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
					"Det skjedde en feil under signatursjekk: " + e.getMessage(), e);
		}
	}


	private String getServerSignaturFromResponse(final HttpResponse response) {
		String serverSignaturString = null;
		Header firstHeader = response.getFirstHeader(X_Digipost_Signature);
		if(firstHeader != null){
			serverSignaturString = firstHeader.getValue();
		}

		if (isBlank(serverSignaturString)) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
					"Mangler " + X_Digipost_Signature + "-header - server-signatur kunne ikke sjekkes");
		}
		return serverSignaturString;
	}

	public X509Certificate lastSertifikat() {
		try {
			InputStream certStream = new ByteArrayInputStream(certificateSupplier.get());

			CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
			X509Certificate sertifikat = (X509Certificate) cf.generateCertificate(certStream);
			if (sertifikat == null) {
				throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
						"Kunne ikke laste Digipost's public key - server-signatur kunne ikke sjekkes");
			}
			return sertifikat;
		} catch (GeneralSecurityException e) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
					"Kunne ikke laste Digiposts public key - server-signatur kunne ikke sjekkes");
		}
	}
}
