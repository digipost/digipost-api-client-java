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

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.*;
import org.apache.commons.lang3.NotImplementedException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.joda.time.DateTime;
import org.xml.sax.helpers.DefaultHandler;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

import static java.lang.Integer.parseInt;
import static no.digipost.api.client.util.MockfriendlyResponse.*;

public class ApiServiceMock implements ApiService {

	private final Map<String, DigipostRequest> REQUESTS = initRequestMap(100);
	private final Marshaller marshaller;

	public ApiServiceMock() {
		this(null);
	}
	public ApiServiceMock(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public DigipostRequest getRequest(String messageId) {
		return REQUESTS.get(messageId);
	}

	@Override
	public EntryPoint getEntryPoint() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Response multipartMessage(final MultiPart multiPart) {
		Message message = null;
		List<ContentPart> contentParts = new ArrayList<>();
		for (BodyPart bodyPart : multiPart.getBodyParts()) {
			if (bodyPart.getMediaType().toString().equals(MediaTypes.DIGIPOST_MEDIA_TYPE_V6)) {
				message = (Message) bodyPart.getEntity();
			} else {
				contentParts.add(new ContentPart(bodyPart.getMediaType()));
			}
		}
		if (message == null) {
			throw new IllegalArgumentException("MultiPart does not contain Message");
		}

		if (marshaller != null) {
			validateAgainstXsd(message);
		}

		Response response;
		String subject = message.getPrimaryDocument().subject;
		if (responses.containsKey(subject)) {
			response = responses.get(subject);
		} else if (errors.containsKey(subject)) {
			throw errors.get(subject);
		} else if (subject.matches("^[0-9]{3}:(.)+")) {
			String[] split = subject.split(":");
			if (ErrorCode.isKnown(split[1])) {
				ErrorCode errorCode = ErrorCode.resolve(split[1]);
				response = MockedResponseBuilder.create().status(parseInt(split[0]))
						.entity(new ErrorMessage(errorCode.getErrorType(), errorCode.name(), "Generic error-message from digipost-api-client-mock")).build();
			} else {
				throw new IllegalArgumentException("ErrorCode " + split[1] + " is unknown");
			}
		} else {
			response = DEFAULT_RESPONSE;
		}
		REQUESTS.put(message.getMessageId(), new DigipostRequest(message, contentParts));
		return response;
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

	@Override
	public Response getDocumentEvents(final String organisation, final String partId, final DateTime from, final DateTime to, final int offset, final int maxResults) {
		throw new NotImplementedException("This is a mock");
	}

	private Map<String, DigipostRequest> initRequestMap(final int maxSize) {
		return Collections.synchronizedMap(new LinkedHashMap<String, DigipostRequest>() {
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > maxSize;
			}
		});
	}

	private void validateAgainstXsd(Message message) {
		try {
			marshaller.marshal(message, new DefaultHandler());
		} catch (JAXBException e) {
			StringWriter w = new StringWriter();
			PrintWriter printWriter = new PrintWriter(w);
			e.printStackTrace(printWriter);
			throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST, "DigipostClientMock failed to marshall Message to xml.\n\n" + w.toString());
		}
	}

	public static class DigipostRequest {

		public final Message message;
		public final List<ContentPart> contentParts;

		public DigipostRequest(Message message, List<ContentPart> contentParts) {
			this.message = message;
			this.contentParts = contentParts;
		}
	}

	public static class ContentPart {

		public final MediaType mediaType;

		public ContentPart(MediaType mediaType) {
			this.mediaType = mediaType;
		}
	}
}
