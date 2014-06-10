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
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class MessageSenderTest {

	@Mock
	Response mockClientResponse;

	@Mock
	Response mockClientResponse2;

	@Before
	public void setUp() {
		initMocks(this);
	}

	@Test
	public void skalHenteEksisterendeForsendelseHvisDenFinnesFraForr() {
		ApiService api = mock(ApiService.class);
		Message forsendelseIn = lagDefaultForsendelse();

		when(mockClientResponse.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.getMessageId(), DeliveryMethod.DIGIPOST, MessageStatus.NOT_COMPLETE, null);

		when(mockClientResponse2.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
		when(mockClientResponse2.readEntity(MessageDelivery.class)).thenReturn(eksisterendeForsendelse);
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

		when(mockClientResponse.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.getMessageId(), DeliveryMethod.DIGIPOST, MessageStatus.DELIVERED,
				DateTime.now());

		when(mockClientResponse2.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
		when(mockClientResponse2.readEntity(MessageDelivery.class)).thenReturn(eksisterendeForsendelse);
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

		when(mockClientResponse.getStatus()).thenReturn(Response.Status.CONFLICT.getStatusCode());
		when(api.createMessage(forsendelseIn)).thenReturn(mockClientResponse);

		MessageDelivery eksisterendeForsendelse = new MessageDelivery(forsendelseIn.getMessageId(), DeliveryMethod.PRINT, MessageStatus.DELIVERED_TO_PRINT,
				DateTime.now());

		when(mockClientResponse2.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
		when(mockClientResponse2.readEntity(MessageDelivery.class)).thenReturn(eksisterendeForsendelse);
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
				null, new SmsNotification(0), null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL), new ArrayList<Document>());
	}
}
