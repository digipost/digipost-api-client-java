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
import no.motif.f.Predicate;
import no.motif.types.Elements;
import org.junit.Test;

import java.lang.reflect.Field;

import static java.lang.reflect.Modifier.*;
import static no.digipost.api.client.representations.sender.SenderFeature.DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK;
import static no.motif.Iterate.on;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
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

	@Test
	public void allSenderFeatureConstantsAreIncludedAsKnownFeatures() throws Exception {
		Elements<Field> declaredConstants = on(SenderFeature.class.getFields())
			.filter(new Predicate<Field>() { @Override public boolean $(Field f) {
					return f.getType() == SenderFeature.class
							&& isStatic(f.getModifiers())
							&& isFinal(f.getModifiers())
							&& isPublic(f.getModifiers());
					}});

		assertFalse(declaredConstants.isEmpty());
		for (Field constantField : declaredConstants) {
			SenderFeature constant = (SenderFeature) constantField.get(SenderFeature.class);
			assertThat(SenderFeature.from(constant.identificator), sameInstance(constant));
		}
	}

}
