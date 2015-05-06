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
package no.digipost.api.client.representations.sender;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static no.digipost.api.client.representations.sender.SenderFeature.DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class SenderFeatureTest {

	@Test
	public void correctEqualsAndHashcode() {
		EqualsVerifier.forClass(SenderFeature.class).verify();
	}

	@Test
	public void customFeatures() {
		assertThat(SenderFeature.from("my.feature"), is(SenderFeature.from("my.feature")));
	}

	@Test
	public void knownFeaturesAreSingletons() {
		assertThat(SenderFeature.from(DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK.identificator),
				sameInstance(DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK));
	}

}
