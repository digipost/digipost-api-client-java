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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.PrintMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PrintOrdererTest {

	@Mock
	private ApiService apiService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldThrowNotAuthorizedExceptionIfCreatePrintMessageLinkIsNotAvailable() {
		when(apiService.getEntryPoint()).thenReturn(new EntryPoint());

		try {
			new PrintOrderer(apiService, DigipostClient.NOOP_EVENT_LOGGER).orderPrintDirectly(new PrintMessage(), null);
		} catch (DigipostClientException e) {
			assertEquals(ErrorType.NOT_AUTHORIZED_FOR_PRINT, e.getErrorType());
			return;
		}

		fail("Should have thrown exception");
	}

}
