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
package no.digipost.api.client.representations;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.newMessage;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ForsendelseRepresentasjonTest {

    @Test
    public void skalHanteraDuplikatForsendelse() {
        DigipostAddress digipostAddress = new DigipostAddress("peter.pan#0000");
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Message adresseForsendelse1 = newMessage(id1, new Document(UUID.randomUUID(), "emne", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .recipient(digipostAddress)
                .build();

        Message adresseForsendelse2 = newMessage(id1, new Document(UUID.randomUUID(), "emne", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .recipient(digipostAddress)
                .build();

        Message adresseForsendelse3 = newMessage(id2, new Document(UUID.randomUUID(), "annetemne", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .recipient(digipostAddress)
                .build();

        assertTrue(adresseForsendelse1.isSameMessageAs(adresseForsendelse2));
        assertTrue(adresseForsendelse2.isSameMessageAs(adresseForsendelse1));

        assertFalse(adresseForsendelse1.isSameMessageAs(adresseForsendelse3));
    }

}
