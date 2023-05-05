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
package no.digipost.api.client.representations.print;

import no.digipost.api.client.representations.PrintRecipient;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static no.digipost.api.client.representations.ObjectBuilder.newNorwegianRecipient;
import static no.digipost.api.client.representations.ObjectBuilder.newPrintMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrintMessageTest {

    @Test
    public void testIsSameMessageAs() {
        PrintRecipient recipient1 = newNorwegianRecipient("Name", "Zip", "City");
        PrintRecipient recipient2 = newNorwegianRecipient("Name2", "Zip2", "City2");
        PrintRecipient returnAddress = newNorwegianRecipient("SenderName", "SenderZip", "SenderCity");

        UUID uniqueId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        assertTrue(newPrintMessage(uniqueId, recipient1, returnAddress).isSameMessageAs(
                newPrintMessage(uniqueId, recipient1, returnAddress)));

        assertTrue(newPrintMessage(uniqueId, recipient1, returnAddress).isSameMessageAs(
                newPrintMessage(uniqueId, recipient1, recipient2)));

        assertFalse(newPrintMessage(uniqueId, recipient1, returnAddress).isSameMessageAs(
                newPrintMessage(otherId, recipient1, returnAddress)));
    }

}
