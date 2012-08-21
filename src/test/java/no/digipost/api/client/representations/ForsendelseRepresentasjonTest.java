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

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ForsendelseRepresentasjonTest {

	@Test
	public void skalHanteraDuplikatForsendelse() {
		DigipostAddress digipostAddress = new DigipostAddress("peter.pan#0000");
		Message adresseForsendelse1 = new Message("id", "emne", digipostAddress, false, PASSWORD, NORMAL);
		Message adresseForsendelse3 = new Message("id", "annetemne", digipostAddress, false, PASSWORD, NORMAL);

		assertTrue(adresseForsendelse1.isSameMessageAs(adresseForsendelse1));
		assertTrue(adresseForsendelse3.isSameMessageAs(adresseForsendelse3));

		assertFalse(adresseForsendelse1.isSameMessageAs(adresseForsendelse3));
	}

}
