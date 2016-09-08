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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.HashMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Agreement {

	@XmlAttribute
	@XmlJavaTypeAdapter(URIXmlAdapter.class)
	private URI href;

	@XmlElement(required = true)
	@XmlJavaTypeAdapter(AgreementTypeXmlAdapter.class)
	private AgreementType type;

	@XmlElement(name = "user-id", required = true)
	private String userId;
	//TODO: Include BankId session identifier?
	//private String sessionId;

	@XmlElement
	@XmlJavaTypeAdapter(AttributesMapAdapter.class)
	// TODO: switch to EnumMap?
	private HashMap<String, String> attributes;

	public Agreement() {}

	public Agreement(final AgreementType type, final UserId userId, final HashMap<String, String> attributes) {
		this.type = type;
		this.userId = userId.getPersonalIdentificationNumber();
		this.attributes = attributes == null ? new HashMap<String, String>() : attributes;
	}

	public static Agreement createInvoiceBankAgreement(final UserId userId, final boolean smsNotification) {
		HashMap<String, String> attribs = new HashMap<>();
		attribs.put("sms-notification", String.valueOf(smsNotification));
		return new Agreement(AgreementType.INVOICE_BANK, userId, attribs);
	}

	public AgreementType getType() {
		return type;
	}

	public UserId getUserId() {
		return new UserId(userId);
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	public URI getHref() {
		return href;
	}

	public void setHref(final URI href) {
		this.href = href;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Agreement{");
		sb.append("href=").append(href);
		sb.append(", type=").append(type);
		sb.append(", userId='").append(userId).append('\'');
		sb.append(", attributes=").append(attributes);
		sb.append('}');
		return sb.toString();
	}
}
