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
import no.digipost.api.client.representations.Message;

public class MessageDeliverer {

	private final MessageSender sender;

	public MessageDeliverer(MessageSender sender) {
		this.sender = sender;
	}


	public OngoingDelivery.WithPrintFallback createMessage(Message message) {
		return new WithPrintFallback(message, sender);

	}

	public OngoingDelivery.ForPrintOnly createPrintOnlyMessage(final Message printMessage) {
		return new PrintOnlyMessage(printMessage, sender);
	}

}
