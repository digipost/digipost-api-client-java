/*
 * Copyright (C) Posten Bring AS
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

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;

class RecipientIdentifierTest {

    @Test
    void correctEqualsAndHashCode() {
        assertAll(
                () -> EqualsVerifier.forClass(RecipientIdentifier.class).verify(),
                () -> EqualsVerifier.forClass(DigipostAddress.class).withRedefinedSuperclass().verify(),
                () -> EqualsVerifier.forClass(BankAccountNumber.class).withRedefinedSuperclass().verify(),
                () -> EqualsVerifier.forClass(OrganisationNumber.class).withRedefinedSuperclass().verify(),
                () -> EqualsVerifier.forClass(PersonalIdentificationNumber.class).withRedefinedSuperclass().verify()
        );
    }
}
