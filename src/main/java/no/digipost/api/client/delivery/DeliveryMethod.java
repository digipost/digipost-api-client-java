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

import no.digipost.api.client.delivery.OngoingDelivery.SendableDelivery;

public enum DeliveryMethod {
	/**
	 * This will deliver messages using a stepwise
	 * variant of the REST-API, using several HTTP requests to build the message,
	 * and ultimately {@link SendableDelivery#send() send} it.
	 */
	STEPWISE_REST,

	/**
	 * This will {@link SendableDelivery#send() send} each message using <em>one</em>
	 * multipart HTTP-request containing all documents and their metadata.
	 * <em>For now this is an experimental feature, and not supported.</em>
	 */
	ATOMIC_REST
}