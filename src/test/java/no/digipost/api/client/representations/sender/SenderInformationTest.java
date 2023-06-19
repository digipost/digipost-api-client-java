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

import no.digipost.api.client.SenderId;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.XmlTestHelper.marshallValidateAndUnmarshall;
import static no.digipost.api.client.representations.sender.SenderFeatureName.DIGIPOST_DELIVERY;
import static no.digipost.api.client.representations.sender.SenderStatus.NO_INFO_AVAILABLE;
import static no.digipost.api.client.representations.sender.SenderStatus.VALID_SENDER;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SenderInformationTest {

    @Test
    public void marshallValidateXmlAndUnmarshall() {
        SenderInformation senderInformation = new SenderInformation(null, NO_INFO_AVAILABLE, Collections.emptyList());
        SenderInformation unmarshalled = marshallValidateAndUnmarshall(senderInformation);
        assertTrue(reflectionEquals(senderInformation, unmarshalled));

        senderInformation = new SenderInformation(SenderId.of(1L), VALID_SENDER, asList(DIGIPOST_DELIVERY.withNoParam(), SenderFeatureName.from("no.digipost.feature.delivery.pizza").withNoParam()));
        unmarshalled = marshallValidateAndUnmarshall(senderInformation);
        assertTrue(reflectionEquals(senderInformation, unmarshalled));
    }


    @Test
    void shouldNotCauseNullpointerException() {
        SenderInformation senderInformation = new SenderInformation(null, NO_INFO_AVAILABLE, Collections.emptyList());
        assertFalse(senderInformation.hasEnabled(DIGIPOST_DELIVERY));
        System.out.println(senderInformation);
    }
}
