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


import java.util.UUID;

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.newMessage;
import static no.digipost.api.client.representations.PrintDetails.NondeliverableHandling.RETURN_TO_SENDER;
import static no.digipost.api.client.representations.PrintDetails.PrintColors.MONOCHROME;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

public class ObjectBuilder {

    public static PrintRecipient newNorwegianRecipient(String name, String zip, String city) {
        return newNorwegianRecipient(name, null, null, null, zip, city);
    }

    public static PrintRecipient newNorwegianRecipient(String name, String address1, String address2, String address3, String zip,
            String city) {
        NorwegianAddress norwegianAddress = new NorwegianAddress(zip, city);
        PrintRecipient printRecipient = new PrintRecipient(name, norwegianAddress);
        norwegianAddress.setAddressline1(address1);
        norwegianAddress.setAddressline2(address2);
        norwegianAddress.setAddressline3(address3);
        return printRecipient;
    }

    public static PrintRecipient newForeignAddress(String name, String addressline1, String country, String countryCode) {
        ForeignAddress foreignAddress = new ForeignAddress(addressline1, country, countryCode);
        return new PrintRecipient(name, foreignAddress);
    }

    public static Message newPrintMessage(UUID messageId, PrintRecipient recipient, PrintRecipient returnAddress) {
        return newMessage(messageId, new Document(UUID.randomUUID(), "emne", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .recipient(new MessageRecipient(new PrintDetails(recipient, returnAddress, MONOCHROME, RETURN_TO_SENDER)))
                .build();
    }

}
