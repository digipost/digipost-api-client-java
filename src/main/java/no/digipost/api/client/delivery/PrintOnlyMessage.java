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
package no.digipost.api.client.delivery;

import no.digipost.api.client.internal.MessageSender;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sender en forsendelse direkte til print gjennom Digipost i ett kall.
 * (Krever at avsender har fått tilgang til print.)
 */
final class PrintOnlyMessage implements OngoingDelivery.SendableForPrintOnly {

    private final MessageSender sender;
    private final Message printMessage;
    private final Map<String, DocumentContent> documents = new LinkedHashMap<>();


    PrintOnlyMessage(Message printMessage, MessageSender sender) {
        if (!printMessage.isDirectPrint()) {
            throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
                    + "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
        }
        this.printMessage = printMessage;
        this.sender = sender;
    }


    /**
     * Laster opp innhold til et dokument. Merk: må være PDF-format.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    @Override
    public PrintOnlyMessage addContent(Document document, InputStream content) {
        documents.put(document.uuid, DocumentContent.CreatePrintContent(content));
        return this;
    }


    @Override
    public MessageDelivery send() {
        return sender.sendMultipartMessage(printMessage, documents);
    }

}

