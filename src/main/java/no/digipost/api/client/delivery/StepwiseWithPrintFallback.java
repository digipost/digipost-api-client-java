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

import no.digipost.api.client.MessageSender;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;

import java.io.InputStream;

final class StepwiseWithPrintFallback implements OngoingDelivery.SendableWithPrintFallback {
    private final MessageSender sender;
    private MessageDelivery delivery;


    StepwiseWithPrintFallback(Message message, MessageSender sender) {
		this.sender = sender;
	    this.delivery = sender.createOrFetchMessage(message);
    }


    /**
     * Laster opp innhold til et dokument.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    @Override
    public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content) {
    	return addContent(document, content, content);
    }


    @Override
    public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content, InputStream printContent) {
    	this.delivery = sender.addContent(delivery, delivery.getDocumentByUuid(document.getUuid()), content, printContent);
    	return this;
    }


    /**
     * Sender forsendelsen gjennom Digipost. Dersom mottaker ikke er Digipostbruker
     * og det ligger printdetaljer på forsendelsen bestiller vi print av brevet
     * til vanlig postgang. (Krever at avsender har fått tilgang til print.)
     */
    @Override
    public MessageDelivery send() {
    	return sender.sendMessage(delivery);
    }
}