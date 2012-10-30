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
package no.digipost.api.client.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message", propOrder = { "deliveredDate", "subject", "recipients", "personalIdentificationNumbers", "smsNotification",
		"authenticationLevel", "sensitivityLevel", "status", "links" })
@XmlRootElement(name = "message")
public class Message extends MessageBase {
	@XmlElement(name = "delivered-date", type = String.class)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	protected DateTime deliveredDate;
	@XmlElement(required = true)
	protected String subject;
	@XmlElement(name = "recipient")
	protected List<Recipient> recipients;
	@XmlElement(name = "personal-identification-number")
	protected List<String> personalIdentificationNumbers;
	@XmlElement(name = "sms-notification")
	protected SmsNotification smsNotification;
	protected MessageStatus status;
	@XmlElement(name = "authentication-level")
	protected AuthenticationLevel authenticationLevel;
	@XmlElement(name = "sensitivity-level")
	protected SensitivityLevel sensitivityLevel;

	Message() {
	}

	public Message(final String messageId, final String subject, final PersonalIdentificationNumber id, final SmsNotification smsVarsling,
			final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, final Link... links) {
		this(messageId, subject, smsVarsling, authenticationLevel, sensitivityLevel, links);
		personalIdentificationNumbers = new ArrayList<String>();
		personalIdentificationNumbers.add(id.asString());
	}

	public Message(final String messageId, final String subject, final DigipostAddress digipostAdress, final SmsNotification smsVarsling,
			final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, final Link... links) {
		this(messageId, subject, smsVarsling, authenticationLevel, sensitivityLevel, links);
		recipients = new ArrayList<Recipient>();
		recipients.add(new Recipient(null, null, null, digipostAdress.asString(), null));
	}

	private Message(final String messageId, final String subject, final SmsNotification smsVarsling, final AuthenticationLevel authenticationLevel,
			final SensitivityLevel sensitivityLevel, final Link... links) {
		super(messageId, links);
		this.subject = subject;
		smsNotification = smsVarsling;
		this.authenticationLevel = authenticationLevel;
		this.sensitivityLevel = sensitivityLevel;
	}

	public String getSubject() {
		return subject;
	}

	public boolean hasSubject() {
		return !StringUtils.isBlank(subject);
	}

	public MessageStatus getStatus() {
		return status;
	}

	public void setStatus(final MessageStatus status) {
		this.status = status;
	}

	public SmsNotification getSmsNotification() {
		return smsNotification;
	}

	public AuthenticationLevel getAuthenticationLevel() {
		return authenticationLevel;
	}

	public SensitivityLevel getSensitivityLevel() {
		return sensitivityLevel;
	}

	public List<Recipient> getRecipients() {
		return recipients;
	}

	public List<String> getPersonalIdentificationNumbers() {
		return personalIdentificationNumbers;
	}

	@XmlElement(name = "link")
	protected List<Link> getLinks() {
		return links;
	}

	protected void setLinks(final List<Link> links) {
		this.links = links;
	}

	@Override
	public boolean isSameMessageAs(final Object message) {
		if (!(message instanceof Message)) {
			return false;
		}
		return messageId.equals(((Message) message).getMessageId()) && subject.equals(((Message) message).getSubject());
	}

}
