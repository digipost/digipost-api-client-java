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
package no.digipost.api.client.util;

import no.digipost.api.client.representations.MessageDelivery;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;

import javax.ws.rs.core.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

import static javax.ws.rs.core.Response.Status.OK;
import static no.digipost.api.client.representations.DeliveryMethod.DIGIPOST;
import static no.digipost.api.client.representations.MessageStatus.COMPLETE;

public class MockfriendlyResponse extends Response {

	public static final Map<String, Response> responses = new HashMap<>();

	public static Response DEFAULT_RESPONSE = MockedResponseBuilder.create()
			.status(OK.getStatusCode())
			.entity(new MessageDelivery(UUID.randomUUID().toString(), DIGIPOST, COMPLETE, DateTime.now()))
			.build();

	static {
		responses.put("200:OK", DEFAULT_RESPONSE);
	}

	public static class MockedResponseBuilder {
		private int status;
		private Object entity;

		public static MockedResponseBuilder create() {
			return new MockedResponseBuilder();
		}

		public MockedResponseBuilder status(final int status) {
			this.status = status;
			return this;
		}

		public MockedResponseBuilder entity(final Object entity) {
			this.entity = entity;
			return this;
		}

		public MockfriendlyResponse build() {
			return new MockfriendlyResponse() {
				@Override
				public int getStatus() {
					return status;
				}

				@Override
				@SuppressWarnings("unchecked")
				public <T> T readEntity(final Class<T> entityType) {
					return (T) entity;
				}
			};
		}
	}

	@Override
	public int getStatus() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public StatusType getStatusInfo() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Object getEntity() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public <T> T readEntity(final Class<T> entityType) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public <T> T readEntity(final GenericType<T> entityType) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public <T> T readEntity(final Class<T> entityType, final Annotation[] annotations) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public <T> T readEntity(final GenericType<T> entityType, final Annotation[] annotations) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public boolean hasEntity() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public boolean bufferEntity() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public void close() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public MediaType getMediaType() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Locale getLanguage() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public int getLength() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Set<String> getAllowedMethods() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Map<String, NewCookie> getCookies() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public EntityTag getEntityTag() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Date getDate() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Date getLastModified() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public URI getLocation() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Set<Link> getLinks() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public boolean hasLink(final String relation) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Link getLink(final String relation) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public Link.Builder getLinkBuilder(final String relation) {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public MultivaluedMap<String, String> getStringHeaders() {
		throw new NotImplementedException("This is a mock");
	}

	@Override
	public String getHeaderString(final String name) {
		throw new NotImplementedException("This is a mock");
	}
}
