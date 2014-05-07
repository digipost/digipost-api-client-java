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

import no.digipost.api.client.ApiService;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.MessageSender;
import no.digipost.api.client.representations.DeliveryMethod;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;

import java.io.InputStream;

final class StepwisePrintOnlyMessage implements OngoingDelivery.SendableForPrintOnly {

	private final MessageSender sender;
    private MessageDelivery delivery;


    StepwisePrintOnlyMessage(Message printMessage, ApiService apiService, EventLogger eventLogger) {
    	if (!printMessage.isDirectPrint()) {
    		throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
    				+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
    	}
    	this.sender = new MessageSender(apiService, eventLogger);
    	this.delivery = sender.createOrFetchMessage(printMessage);
    }


    /**
     * Laster opp innhold til et dokument. Merk: må være PDF-format.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    @Override
    public OngoingDelivery.SendableForPrintOnly addContent(Document document, InputStream printContent) {
    	this.delivery = sender.addContent(delivery, delivery.getDocumentByUuid(document.getUuid()), null, printContent);
    	return this;
    }


    @Override
    public MessageDelivery send() {
    	if (delivery.getDeliveryMethod() != DeliveryMethod.PRINT) {
    		throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
    				+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
    	}
    	return sender.sendMessage(delivery);
    }
}