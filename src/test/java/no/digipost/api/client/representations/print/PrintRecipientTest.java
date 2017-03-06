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
package no.digipost.api.client.representations.print;

import static org.junit.Assert.assertTrue;
import static no.digipost.api.client.representations.ObjectBuilder.newForeignAddress;
import static no.digipost.api.client.representations.ObjectBuilder.newNorwegianRecipient;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class PrintRecipientTest {

    @Test
    public void testSameRecipientAsWithNorwegianAddress() {
        assertTrue(newNorwegianRecipient("Name", "Address1", "Address2", "Address3", "Zip", "City").isSameRecipientAs(
                newNorwegianRecipient("Name", "Address1", "Address2", "Address3", "Zip", "City")));

        assertTrue(newNorwegianRecipient("Name", "Zip", "City").isSameRecipientAs(
                newNorwegianRecipient("Name", "Address1", "Address2", "Address3", "Zip", "City")));

        assertTrue(newNorwegianRecipient("Name ", " Zip", " City ").isSameRecipientAs(
                newNorwegianRecipient("Name", "Address1", "Address2", "Address3", "Zip", "City")));

        assertFalse(newNorwegianRecipient("Other name", "Zip", "City").isSameRecipientAs(newNorwegianRecipient("Name", "Zip", "City")));

        assertFalse(newNorwegianRecipient("Name", "OtherZip", "City").isSameRecipientAs(newNorwegianRecipient("Name", "Zip", "City")));
    }

    @Test
    public void testSameRecipientAsWithForeignAddress() {
        assertTrue(newForeignAddress("Name", "Address2", "Sverige", null).isSameRecipientAs(
                newForeignAddress("Name", "Address2", "Sverige", null)));

        assertFalse(newForeignAddress("Name", "Address1", "Sverige", null).isSameRecipientAs(
                newForeignAddress("Name", "Address2", "Sverige", null)));

        assertTrue(newForeignAddress("Name", "Address2", null, "SE").isSameRecipientAs(
                newForeignAddress("Name", "Address2", null, "SE")));

        assertFalse(newForeignAddress("Name", "Address1", "Sverige", null).isSameRecipientAs(
                newForeignAddress("Name", "Address1", "Danmark", null)));

        assertFalse(newForeignAddress("Name", "Address2", null, "DK").isSameRecipientAs(
                newForeignAddress("Name", "Address2", null, "SE")));
    }

}
