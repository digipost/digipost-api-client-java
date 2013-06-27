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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import no.digipost.api.client.representations.AuthenticationLevel;
import no.digipost.api.client.representations.DeliveryMethod;
import no.digipost.api.client.representations.FileType;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.MessageStatus;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.SensitivityLevel;

import no.digipost.api.client.representations.SmsNotification;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class MessageSenderTest {

	@Before
	public void setUp() {
	}

	@Test
	public void skalHenteEksisterendeForsendelseHvisDenFinnesFraForr() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		when(api.createMessage(forsendelseIn)).thenReturn(new MockClientResponse(Status.CONFLICT));

		MessageDelivery eksisterendeForsendelse = new MessageDelivery("id", DeliveryMethod.DIGIPOST, MessageStatus.NOT_COMPLETE, null);
		when(api.fetchExistingMessage((URI) any())).thenReturn(new MockClientResponse(Status.OK, eksisterendeForsendelse));

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);
		MessageDelivery forsendelse = brevSender.createOrFetchMessage(forsendelseIn);

		verify(api).fetchExistingMessage((URI) any());
		assertTrue(forsendelse.isSameMessageAs(forsendelseIn));
	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevert() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		when(api.createMessage(forsendelseIn)).thenReturn(new MockClientResponse(Status.CONFLICT));

		MessageDelivery eksisterendeForsendelse = new MessageDelivery("id", DeliveryMethod.DIGIPOST, MessageStatus.DELIVERED,
				DateTime.now());
		when(api.fetchExistingMessage((URI) any())).thenReturn(new MockClientResponse(Status.OK, eksisterendeForsendelse));

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);

		try {
			brevSender.createOrFetchMessage(forsendelseIn);
			fail();
		} catch (DigipostClientException e) {
			assertEquals(ErrorType.DIGIPOST_MESSAGE_ALREADY_DELIVERED, e.getErrorType());
		}

	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevertTilPrint() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		when(api.createMessage(forsendelseIn)).thenReturn(new MockClientResponse(Status.CONFLICT));

		MessageDelivery eksisterendeForsendelse = new MessageDelivery("id", DeliveryMethod.PRINT, MessageStatus.DELIVERED_TO_PRINT,
				DateTime.now());
		when(api.fetchExistingMessage((URI) any())).thenReturn(new MockClientResponse(Status.OK, eksisterendeForsendelse));

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);

		try {
			brevSender.createOrFetchMessage(forsendelseIn);
			fail();
		} catch (DigipostClientException e) {
			assertEquals(ErrorType.PRINT_MESSAGE_ALREADY_DELIVERED, e.getErrorType());
		}

	}

	private Message lagDefaultForsendelse() {
		return lagEnkeltForsendelse("emne", "id", "12345678900");
	}

	private Message lagEnkeltForsendelse(final String subject, final String messageId, final String fnr) {
		return new Message(messageId, subject, new PersonalIdentificationNumber(fnr), new SmsNotification(0), AuthenticationLevel.PASSWORD,
				SensitivityLevel.NORMAL, FileType.PDF);
	}

	private class MockClientResponse extends ClientResponse {

		private final MessageDelivery eksisterendeForsendelse;

		public MockClientResponse(final Status responseStatus) {
			super(responseStatus.getStatusCode(), null, null, null);
			eksisterendeForsendelse = null;
		}

		public MockClientResponse(final Status responseStatus, final MessageDelivery eksisterendeForsendelse) {
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
