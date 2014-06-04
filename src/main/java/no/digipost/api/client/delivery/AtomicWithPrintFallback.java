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
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;

import java.io.InputStream;

/**
 * Sender en forsendelse gjennom Digipost i ett kall. Dersom mottaker ikke er
 * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
 * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
 * til print.)
 */
final class AtomicWithPrintFallback extends MultipartSendMessage implements OngoingDelivery.SendableWithPrintFallback {


    AtomicWithPrintFallback(Message message, ApiService apiService, EventLogger eventLogger) {
    	super(message, apiService, eventLogger);
    }


    /**
     * Laster opp innhold til et dokument.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    @Override
    public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content) {
		if(document.isPreEncrypt()) {
			throw new UnsupportedOperationException(
					"Pre-encrypt is not supported for " + DeliveryMethod.class.getSimpleName() + " " + DeliveryMethod.ATOMIC_REST);
		}
    	add(document, content);
    	return this;
    }


    @Override
    public OngoingDelivery.SendableWithPrintFallback addContent(Document document, InputStream content, InputStream printContent) {
    	if (printContent == null) {
    		return addContent(document, content);
    	} else {
    		throw new UnsupportedOperationException(
    				"Adding separate content for print is not supported for " +
					DeliveryMethod.class.getSimpleName() + " " + DeliveryMethod.ATOMIC_REST);
    	}
    }

}