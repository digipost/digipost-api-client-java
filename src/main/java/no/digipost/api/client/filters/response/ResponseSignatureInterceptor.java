package no.digipost.api.client.filters.response;

import no.digipost.api.client.ApiService;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.security.ClientResponseToVerify;
import no.digipost.api.client.security.ClientResponseToVerify2;
import no.digipost.api.client.security.ResponseMessageSignatureUtil2;
import no.digipost.api.client.util.LoggingUtil;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

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
import static no.digipost.api.client.security.ResponseMessageSignatureUtil.getCanonicalResponseRepresentation;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseSignatureInterceptor implements HttpResponseInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(ResponseSignatureFilter.class);
	private boolean shouldThrow = true;

	public void setThrowOnError(final boolean shouldThrow) {
		this.shouldThrow = shouldThrow;
	}

	private final EventLogger eventLogger;
	private final ApiService apiService;

	public ResponseSignatureInterceptor(final ApiService apiService) {
		this(NOOP_EVENT_LOGGER, apiService);
	}

	public ResponseSignatureInterceptor(final EventLogger eventLogger, final ApiService apiService) {
		this.eventLogger = eventLogger;
		this.apiService = apiService;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		// TODO configure this on relevant WebTarget instead
		if ("/".equals(((HttpRequestWrapper)context.getAttribute("http.request")).getURI().getPath())) {
			eventLogger.log("Verifiserer ikke signatur fordi det er rotressurs vi hentet.");
			return;
		}

		try {
			String serverSignaturBase64 = getServerSignaturFromResponse(response);
			byte[] serverSignaturBytes = Base64.decode(serverSignaturBase64.getBytes());

			String signatureString = ResponseMessageSignatureUtil2.getCanonicalResponseRepresentation(new ClientResponseToVerify2(context, response));

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
			//LoggingUtil.logResponse(clientResponseContext);
			if (shouldThrow) {
				if (e instanceof DigipostClientException) {
					throw (DigipostClientException) e;
				} else {
					throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
							"Det skjedde en feil under signatursjekk: " + e.getMessage());
				}
			} else {
				LOG.warn("Feil under validering av server signatur: '" + e.getMessage() + "'. " +
						(LOG.isDebugEnabled() ? "" : "Konfigurer debug-logging for " + LOG.getName() + " for å se full stacktrace."));
				LOG.debug(e.getMessage(), e);
			}
		}
	}


	private String getServerSignaturFromResponse(final HttpResponse response) {
		String serverSignaturString = "";

		for(Header head : response.getAllHeaders()){
			if(head.getName().equals(X_Digipost_Signature)){
				serverSignaturString = head.getValue();
				break;
			}
		}

		if (serverSignaturString.equals("") || isBlank(serverSignaturString)) {
			throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
					"Mangler " + X_Digipost_Signature + "-header - server-signatur kunne ikke sjekkes");
		}
		return serverSignaturString;
	}

	public X509Certificate lastSertifikat() {
		try {
			InputStream certStream = new ByteArrayInputStream(apiService.getEntryPoint().getCertificate().getBytes());

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
