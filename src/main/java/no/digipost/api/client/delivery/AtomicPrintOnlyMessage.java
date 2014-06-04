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
final class AtomicPrintOnlyMessage extends MultipartSendMessage implements OngoingDelivery.SendableForPrintOnly {


    AtomicPrintOnlyMessage(Message printMessage, ApiService apiService, EventLogger eventLogger) {
    	super(printMessage, apiService, eventLogger);
    	if (!printMessage.isDirectPrint()) {
    		throw new IllegalArgumentException("Direct print messages must have PrintDetails and "
    				+ "cannot have DigipostAddress, PersonalIdentificationNumber or NameAndAddress");
    	}
    }


    /**
     * Laster opp innhold til et dokument. Merk: må være PDF-format.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    @Override
    public AtomicPrintOnlyMessage addContent(Document document, InputStream content) {
		if(document.isPreEncrypt()) {
			throw new UnsupportedOperationException(
					"Pre-encrypt is not supported for " + DeliveryMethod.class.getSimpleName() + " " + DeliveryMethod.ATOMIC_REST);
		}
    	add(document, content);
    	return this;
    }

}