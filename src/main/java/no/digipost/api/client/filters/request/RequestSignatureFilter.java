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

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Security;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.Headers;
import no.digipost.api.client.security.ClientRequestToSign;
import no.digipost.api.client.security.MessageSignatureUtil;
import no.digipost.api.client.security.Signer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.AbstractClientRequestAdapter;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class RequestSignatureFilter extends ClientFilter {

	private static final Logger LOG = LoggerFactory.getLogger(RequestSignatureFilter.class);

	private final Signer signer;

	private final EventLogger eventLogger;

	public RequestSignatureFilter(final Signer signer) {
		this(signer, NOOP_EVENT_LOGGER);
	}

	public RequestSignatureFilter(final Signer signer, final EventLogger eventListener) {
		this.signer = signer;
		eventLogger = eventListener != null ? eventListener : NOOP_EVENT_LOGGER;
	}

	@Override
	public ClientResponse handle(final ClientRequest cr) {
		if (cr.getEntity() == null) {
			setSignatureHeader(cr);
		} else {
			cr.setAdapter(new SikkerhetsAdapter(cr.getAdapter()));
		}
		return getNext().handle(cr);
	}

	private void setSignatureHeader(final ClientRequest request) {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String stringToSign = MessageSignatureUtil.getCanonicalRequestRepresentation(new ClientRequestToSign(request));
		log(getClass().getSimpleName() + " beregnet streng som skal signeres:\n===START SIGNATURSTRENG===\n" + stringToSign
				+ "===SLUTT SIGNATURSTRENG===");

		byte[] signatureBytes = signer.sign(stringToSign);
		String signature = new String(Base64.encodeBase64(signatureBytes));
		request.getHeaders().add(Headers.X_Digipost_Signature, signature);
		log(getClass().getSimpleName() + " satt headeren " + Headers.X_Digipost_Signature + "=" + signature);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	private final class SikkerhetsAdapter extends AbstractClientRequestAdapter {
		SikkerhetsAdapter(final ClientRequestAdapter cra) {
			super(cra);
		}

		@Override
		public OutputStream adapt(final ClientRequest request, final OutputStream out) throws IOException {
			return new SecurityAdapterOutputStream(request, getAdapter().adapt(request, out));
		}
	}

	private final class SecurityAdapterOutputStream extends OutputStream {

		private final ByteArrayOutputStream byteArrayOutputStream;
		private final OutputStream jerseyStream;
		private final ClientRequest request;

		public SecurityAdapterOutputStream(final ClientRequest request, final OutputStream jerseyStream) {
			this.request = request;
			this.jerseyStream = jerseyStream;

			byteArrayOutputStream = new ByteArrayOutputStream();
		}

		@Override
		public void write(final int b) throws IOException {
			byteArrayOutputStream.write(b);
		}

		@Override
		public void close() throws IOException {
			setSignatureHeader(request);
			writeTemporarilyToJerseystream();
			jerseyStream.close();
			super.close();
		}

		private void writeTemporarilyToJerseystream() throws IOException {
			IOUtils.write(byteArrayOutputStream.toByteArray(), jerseyStream);
			byteArrayOutputStream.close();
		}

	}
}