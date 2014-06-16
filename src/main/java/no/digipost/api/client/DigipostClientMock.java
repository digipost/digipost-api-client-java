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
package no.digipost.api.client;

import no.digipost.api.client.delivery.DeliveryMethod;
import no.digipost.api.client.security.Signer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Instansierer en DigipostClient som ikke går mot faktiskt Digipost REST-api endepunkt og
 * som kan brukes av tester o.l for å framprovosere ulike feilsituasjoner.
 */
public class DigipostClientMock {

	private final DigipostClient client;
	private final ApiServiceMock apiService;

	public DigipostClientMock(DeliveryMethod deliveryMethod) {
		if (deliveryMethod == DeliveryMethod.STEPWISE_REST) {
			throw new RuntimeException("Stepwise REST is not yet supported by " + DigipostClientMock.class.getName());
		}
		apiService = new ApiServiceMock(initMarshaller());
		client = new DigipostClient(deliveryMethod, "digipostmock-url", 1, new Signer() {

			@Override
			public byte[] sign(String dataToSign) {
				return new byte[0];
			}
		}, apiService);
	}

	public DigipostClient getClient() {
		return client;
	}

	public ApiServiceMock.DigipostRequest getRequest(String messageId) {
		return apiService.getRequest(messageId);
	}

	/**
	 * Used to validate that requests are according to XSD
	 */
	private static Marshaller initMarshaller() {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		Marshaller marshaller;
		try {
			schema = schemaFactory.newSchema(DigipostClient.class.getResource("/xsd/api_v6.xsd"));
			marshaller = JAXBContext.newInstance("no.digipost.api.client.representations").createMarshaller();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		marshaller.setSchema(schema);
		return marshaller;
	}

}
