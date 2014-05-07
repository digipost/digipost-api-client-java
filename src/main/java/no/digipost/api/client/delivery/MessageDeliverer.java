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
import no.digipost.api.client.representations.Message;

public class MessageDeliverer {

	private final DeliveryMethod type;
	private final EventLogger eventLogger;
	private final ApiService apiService;

	public MessageDeliverer(DeliveryMethod type, ApiService apiService, EventLogger eventLogger) {
		this.type = type;
		this.apiService = apiService;
		this.eventLogger = eventLogger;
	}


	public OngoingDelivery.WithPrintFallback createMessage(Message message) {
		switch (type) {
			case STEPWISE_REST: return new StepwiseWithPrintFallback(message, apiService, eventLogger);
			case ATOMIC_REST: return new AtomicWithPrintFallback(message, apiService, eventLogger);
			default: throw new UnsupportedOperationException(DeliveryMethod.class.getSimpleName() + " " + type + " is not supported");
		}
	}

	public OngoingDelivery.ForPrintOnly createPrintOnlyMessage(final Message printMessage) {
		switch (type) {
    		case STEPWISE_REST: return new StepwisePrintOnlyMessage(printMessage, apiService, eventLogger);
    		case ATOMIC_REST: return new AtomicPrintOnlyMessage(printMessage, apiService, eventLogger);
    		default: throw new UnsupportedOperationException(DeliveryMethod.class.getSimpleName() + " " + type + " is not supported");
    	}
	}

}
