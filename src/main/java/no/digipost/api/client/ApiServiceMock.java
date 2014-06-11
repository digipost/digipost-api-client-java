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
package no.digipost.api.client;

import no.digipost.api.client.representations.*;
import org.apache.commons.lang3.NotImplementedException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;

import static no.digipost.api.client.util.MockfriendlyResponse.DEFAULT_RESPONSE;
import static no.digipost.api.client.util.MockfriendlyResponse.responses;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public class ApiServiceMock implements ApiService {
	@Override
	public EntryPoint getEntryPoint() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Response multipartMessage(final MultiPart multiPart) {
		Message message = null;
		for (BodyPart bodyPart : multiPart.getBodyParts()) {
			if (bodyPart.getMediaType().toString().equals(MediaTypes.DIGIPOST_MEDIA_TYPE_V6)) {
				message = (Message) bodyPart.getEntity();
				break;
			}
		}
		if (message == null) {
			throw new IllegalArgumentException("MultiPart does not contain Message");
		}

		return defaultIfNull(responses.get(message.getPrimaryDocument().subject), DEFAULT_RESPONSE);
	}

	@Override
	public Response createMessage(final Message message) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Response fetchExistingMessage(final URI location) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Response getEncryptionKey(final URI location) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Response addContent(final Document document, final InputStream letterContent) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Response send(final MessageDelivery createdMessage) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Recipients search(final String searchString) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Autocomplete searchSuggest(final String searchString) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public void addFilter(final ClientRequestFilter filter) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public IdentificationResult identifyRecipient(final Identification identification) {
		throw new NotImplementedException("This is a mock");
	}
}
