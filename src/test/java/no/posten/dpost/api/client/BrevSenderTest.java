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
package no.posten.dpost.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import no.digipost.api.client.ApiService;
import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.MessageSender;
import no.digipost.api.client.DigipostClientException.ErrorType;
import no.digipost.api.client.representations.AuthenticationLevel;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageStatus;
import no.digipost.api.client.representations.PersonalIdentificationNumber;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class BrevSenderTest {

	@Before
	public void setUp() {
	}

	@Test
	public void skalHenteEksisterendeForsendelseHvisDenFinnesFraForr() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		when(api.createMessage(forsendelseIn)).thenReturn(new MockClientResponse(Status.CONFLICT));

		Message eksisterendeForsendelse = lagDefaultForsendelse();
		eksisterendeForsendelse.setStatus(MessageStatus.EXPECTING_CONTENT);
		when(api.fetchExistingMessage((URI) any())).thenReturn(new MockClientResponse(Status.OK, eksisterendeForsendelse));

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);
		Message forsendelse = brevSender.createOrFetchMessage(forsendelseIn);

		verify(api).fetchExistingMessage((URI) any());
		assertTrue(forsendelse.isIdenticalTo(forsendelseIn));
	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevert() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		when(api.createMessage(forsendelseIn)).thenReturn(new MockClientResponse(Status.CONFLICT));

		Message eksisterendeForsendelse = lagDefaultForsendelse();
		eksisterendeForsendelse.setStatus(MessageStatus.DELIVERED);
		when(api.fetchExistingMessage((URI) any())).thenReturn(new MockClientResponse(Status.OK, eksisterendeForsendelse));

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);

		try {
			brevSender.createOrFetchMessage(forsendelseIn);
		} catch (Exception e) {
			assertTrue(e instanceof DigipostClientException);
			DigipostClientException de = (DigipostClientException) e;
			assertEquals(ErrorType.MESSAGE_DELIVERED, de.getErrorType());
		}

	}

	private Message lagDefaultForsendelse() {
		return lagEnkeltForsendelse("emne", "id", "12345678900");
	}

	private Message lagEnkeltForsendelse(final String subject, final String messageId, final String fnr) {
		return new Message(messageId, subject, new PersonalIdentificationNumber(fnr), true, AuthenticationLevel.PASSWORD);
	}

	private class MockClientResponse extends ClientResponse {

		private final Message eksisterendeForsendelse;

		public MockClientResponse(final Status responseStatus) {
			super(responseStatus.getStatusCode(), null, null, null);
			eksisterendeForsendelse = null;
		}

		public MockClientResponse(final Status responseStatus, final Message eksisterendeForsendelse) {
			super(responseStatus.getStatusCode(), null, null, null);
			this.eksisterendeForsendelse = eksisterendeForsendelse;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getEntity(final Class<T> c) throws ClientHandlerException, UniformInterfaceException {
			return (T) eksisterendeForsendelse;
		}

		@Override
		public URI getLocation() {
			try {
				return new URI("http://localhost/forsendelse/2");
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
		}

	}

}
