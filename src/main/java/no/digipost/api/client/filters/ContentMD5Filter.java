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
package no.digipost.api.client.filters;

import static no.digipost.api.client.DigipostClient.NOOP_EVENT_LOGGER;
import static no.digipost.api.client.Headers.Content_MD5;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import no.digipost.api.client.EventLogger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.AbstractClientRequestAdapter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

public class ContentMD5Filter extends ClientFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ContentMD5Filter.class);
	private final EventLogger eventLogger;

	public ContentMD5Filter(final EventLogger eventListener) {
		eventLogger = eventListener != null ? eventListener : NOOP_EVENT_LOGGER;
	}

	public ContentMD5Filter() {
		this(NOOP_EVENT_LOGGER);
	}

	@Override
	public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
		cr.setAdapter(new ContentMD5Adapter(cr.getAdapter()));
		return getNext().handle(cr);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	private final class ContentMD5Adapter extends AbstractClientRequestAdapter {
		ContentMD5Adapter(final ClientRequestAdapter cra) {
			super(cra);
		}

		@Override
		public OutputStream adapt(final ClientRequest request, final OutputStream out) throws IOException {
			return new ContentMD5OutputStream(request, getAdapter().adapt(request, out));
		}
	}

	private final class ContentMD5OutputStream extends OutputStream {

		private final ByteArrayOutputStream byteArrayOutputStream;
		private final OutputStream jerseyStream;
		private final ClientRequest request;

		public ContentMD5OutputStream(final ClientRequest request, final OutputStream jerseyStream) {
			this.jerseyStream = jerseyStream;
			this.request = request;

			byteArrayOutputStream = new ByteArrayOutputStream();
		}

		@Override
		public void write(final int b) throws IOException {
			byteArrayOutputStream.write(b);
		}

		@Override
		public void close() throws IOException {
			byteArrayOutputStream.close();
			byte[] byteArray = byteArrayOutputStream.toByteArray();
			settContentMD5Header(byteArray);
			IOUtils.write(byteArray, jerseyStream);
			jerseyStream.close();
		}

		private void settContentMD5Header(final byte[] data) {
			try {
				MessageDigest instance = MessageDigest.getInstance("MD5");
				String contentMD5 = new String(Base64.encodeBase64(instance.digest(data)));
				request.getHeaders().add(Content_MD5, contentMD5);
				log(ContentMD5Filter.class.getSimpleName() + " satt headeren " + Content_MD5 + "=" + contentMD5);
			} catch (NoSuchAlgorithmException e) {
				log("Feil ved generering av Content-MD5");
			}
		}
	}
}
