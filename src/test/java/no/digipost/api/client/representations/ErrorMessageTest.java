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
package no.digipost.api.client.representations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.ErrorType;

import org.junit.Test;

public class ErrorMessageTest {

	@Test
	public void shouldThrowExceptionIfCreatePrintMessageLinkNotAvailable() {
		try {
			new ErrorMessage("Error").getCreatePrintMessageLink();
		} catch (DigipostClientException e) {
			assertEquals(ErrorType.NOT_AUTHORIZED_FOR_PRINT, e.getErrorType());
			return;
		}
		fail("Should have thrown not authorized exception when the create print message link is not available.");
	}

}
