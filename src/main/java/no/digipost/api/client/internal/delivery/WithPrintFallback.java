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
package no.digipost.api.client.internal.delivery;

import no.digipost.api.client.delivery.OngoingDelivery;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sender en forsendelse gjennom Digipost i ett kall. Dersom mottaker ikke er
 * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
 * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
 * til print.)
 */
final class WithPrintFallback implements OngoingDelivery.SendableWithPrintFallback {

    private final MessageDeliverer sender;
    private final Message message;
    private final Map<UUID, DocumentContent> documents = new LinkedHashMap<>();

    WithPrintFallback(Message message, MessageDeliverer sender) {
        this.message = message;
        this.sender = sender;
    }


    /**
     * Laster opp innhold til et dokument.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    @Override
    public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content) {
        documents.put(document.uuid, DocumentContent.CreateBothStreamContent(content));
        return this;
    }

    @Override
    public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content, InputStream printContent) {
        documents.put(document.uuid, DocumentContent.CreateMultiStreamContent(content, printContent));
        return this;
    }

    @Override
    public MessageDelivery send() {
        return sender.sendMultipartMessage(message, documents);
    }
}

