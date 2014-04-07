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
import java.util.ArrayList;
import java.util.UUID;

import no.digipost.api.client.representations.*;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.*;
import javax.ws.rs.core.Link;

public class MessageSenderTest {

	@Before
	public void setUp() {
	}

	/*@Test
	public void skalHenteEksisterendeForsendelseHvisDenFinnesFraForr() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		MockClientResponse mockClientResponse = new MockClientResponse(Response.Status.CONFLICT);
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.getMessageId(), DeliveryMethod.DIGIPOST, MessageStatus.NOT_COMPLETE, null);
		MockClientResponse mockClientResponse2 = new MockClientResponse(Response.Status.OK, eksisterendeForsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);
		MessageDelivery forsendelse = brevSender.createOrFetchMessage(forsendelseIn);

		verify(api).fetchExistingMessage((URI) any());
		assertTrue(forsendelse.isSameMessageAs(forsendelseIn));
	}

	@Test
	public void skalKasteFeilHvisForsendelseAlleredeLevert() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();
		MockClientResponse mockClientResponse = new MockClientResponse(Response.Status.CONFLICT);
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.getMessageId(), DeliveryMethod.DIGIPOST, MessageStatus.DELIVERED,
				DateTime.now());
		MockClientResponse mockClientResponse2 = new MockClientResponse(Response.Status.OK, eksisterendeForsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

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
		MockClientResponse mockClientResponse = new MockClientResponse(Response.Status.CONFLICT, null);
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.getMessageId(), DeliveryMethod.PRINT, MessageStatus.DELIVERED_TO_PRINT,
				DateTime.now());
		MockClientResponse mockClientResponse2 = new MockClientResponse(Response.Status.OK, eksisterendeForsendelse);
		when(api.fetchExistingMessage((URI) any())).thenReturn(mockClientResponse2);

		MessageSender brevSender = new MessageSender(api, DigipostClient.NOOP_EVENT_LOGGER);

		try {
			brevSender.createOrFetchMessage(forsendelseIn);
			fail();
		} catch (DigipostClientException e) {
			assertEquals(ErrorType.PRINT_MESSAGE_ALREADY_DELIVERED, e.getErrorType());
		}

	}

	private Message lagDefaultForsendelse() {
		return lagEnkeltForsendelse("emne", UUID.randomUUID().toString(), "12345678900");
	}

	private Message lagEnkeltForsendelse(final String subject, final String messageId, final String fnr) {
		return new Message(messageId, new PersonalIdentificationNumber(fnr), new Document(UUID.randomUUID().toString(), subject, FileType.PDF,
				null, new SmsNotification(0), AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL), new ArrayList<Document>());
	}

	private class MockClientResponse extends Response {

		private final MessageDelivery eksisterendeForsendelse;

		public MockClientResponse(final Response.Status responseStatus) {
			super(responseStatus, mock(ClientRequest.class));
			eksisterendeForsendelse = null;
		}

		public MockClientResponse(final Response.Status responseStatus, final MessageDelivery eksisterendeForsendelse) {
			super(responseStatus, mock(ClientRequest.class));
			this.eksisterendeForsendelse = eksisterendeForsendelse;
		}

		@Override
		public <T> T readEntity(final Class<T> c) {
			return (T) eksisterendeForsendelse;
		}

		@Override
		public void close() {
			//To change body of implemented methods use File | Settings | File Templates.
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

		@Override
		public Link.Builder getLinkBuilder(String s) {
			return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

	}  */

}
