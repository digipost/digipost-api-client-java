/*
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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.stream.Collectors.toList;
import static no.digipost.api.client.representations.sender.SenderFeatureName.DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class SenderFeatureNameTest {

    @Test
    public void correctEqualsAndHashcode() {
        EqualsVerifier.forClass(SenderFeatureName.class).verify();
    }

    @Test
    public void customFeatures() {
        assertThat(SenderFeatureName.from("my.feature"), is(SenderFeatureName.from("my.feature")));
    }

    @Test
    public void knownFeaturesAreSingletons() {
        assertThat(SenderFeatureName.from(DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK.identificator),
                sameInstance(DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK));
    }

    @Test
    public void allSenderFeatureConstantsAreIncludedAsKnownFeatures() throws Exception {
        List<Field> declaredConstants = Stream.of(SenderFeatureName.class.getFields())
            .filter(f -> f.getType() == SenderFeatureName.class &&
                         isStatic(f.getModifiers()) &&
                         isFinal(f.getModifiers()) &&
                         isPublic(f.getModifiers()))
            .collect(toList());

        for (Field constantField : declaredConstants) {
            SenderFeatureName constant = (SenderFeatureName) constantField.get(SenderFeatureName.class);
            assertThat(SenderFeatureName.from(constant.identificator), sameInstance(constant));
        }
        assertThat(declaredConstants, not(empty()));
    }

}
