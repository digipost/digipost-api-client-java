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
package no.digipost.api.client.userdocuments;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.HashMap;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
public class Agreement {

	public static String INVOICE_BANK_TYPE = "invoice-bank";

	@XmlElement(required = true)
	private String type;
	@XmlElement(name = "user-id", required = true)
	private String userId;
	//private String sessionId;
	//TODO: Include BankId session identifier?
	@XmlElement
	private Map<String, String> attributes;

	public Agreement() {}

	public Agreement(final String type, final String userId, final Map<String, String> attributes) {
		this.type = type;
		this.userId = userId;
		this.attributes = attributes;
	}

	public static Agreement createInvoiceBankAgreement(final String userId, final boolean smsNotification) {
		Map<String, String> attribs = new HashMap<>();
		attribs.put("sms-notification", String.valueOf(smsNotification));
		return new Agreement(INVOICE_BANK_TYPE, userId, attribs);
	}
}
