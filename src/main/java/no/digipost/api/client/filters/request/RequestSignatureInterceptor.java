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
package no.digipost.api.client.filters.request;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.Headers;
import no.digipost.api.client.security.ClientRequestToSign;
import no.digipost.api.client.security.RequestMessageSignatureUtil;
import no.digipost.api.client.security.Signer;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.security.Security;

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;

public class RequestSignatureInterceptor implements HttpRequestInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(RequestSignatureInterceptor.class);

	private final Signer signer;
	private final EventLogger eventListener;
	private final RequestContentHashFilter hashFilter;
	private final EventLogger eventLogger;

	public RequestSignatureInterceptor(final Signer signer, final RequestContentHashFilter hashFilter) {
		this(signer, NOOP_EVENT_LOGGER, hashFilter);
	}

	public RequestSignatureInterceptor(final Signer signer, final EventLogger eventListener, final RequestContentHashFilter hashFilter){
		eventLogger = eventListener != null ? eventListener : NOOP_EVENT_LOGGER;
		this.signer = signer;
		this.eventListener = eventListener;
		this.hashFilter = hashFilter;
	}

	private void setSignatureHeader(final HttpRequest httpRequest) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String stringToSign = RequestMessageSignatureUtil.getCanonicalRequestRepresentation(new ClientRequestToSign(httpRequest));
		log(getClass().getSimpleName() + " beregnet streng som skal signeres:\n===START SIGNATURSTRENG===\n" + stringToSign
				+ "===SLUTT SIGNATURSTRENG===");

		byte[] signatureBytes = signer.sign(stringToSign);
		String signature = new String(Base64.encode(signatureBytes));
		httpRequest.setHeader(Headers.X_Digipost_Signature, signature);
		log(getClass().getSimpleName() + " satt headeren " + Headers.X_Digipost_Signature + "=" + signature);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	@Override
	public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

		if(httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) httpRequest;
			HttpEntity rqEntity = request.getEntity();

			if (rqEntity == null) {
				setSignatureHeader(httpRequest);
			} else {
				byte[] entityBytes = IOUtils.toByteArray(rqEntity.getContent());
				hashFilter.settContentHashHeader(entityBytes, request);
				setSignatureHeader(httpRequest);
			}
		} else {
			setSignatureHeader(httpRequest);
		}
		httpContext.setAttribute("request-path", URI.create(httpRequest.getRequestLine().getUri()).getPath());


	}
}
