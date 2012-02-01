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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message", propOrder = { "messageId", "senderId", "deliveredDate", "subject", "recipients",
		"personalIdentificationNumbers", "smsNotification", "preEncrypt", "status", "links", "authenticationLevel" })
@XmlRootElement(name = "message")
public class Message extends Representation {
	@XmlElement(required = true)
	protected String messageId;
	protected Long senderId;
	@XmlElement(type = String.class)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	protected DateTime deliveredDate;
	@XmlElement(required = true)
	protected String subject;
	@XmlElement(name = "recipient")
	protected List<Recipient> recipients;
	@XmlElement(name = "personalIdentificationNumber")
	protected List<String> personalIdentificationNumbers;
	protected boolean smsNotification;
	protected Boolean preEncrypt;
	protected MessageStatus status;
	protected AuthenticationLevel authenticationLevel;

	Message() {
	}

	public Message(final String messageId, final String subject, final PersonalIdentificationNumber id, final boolean smsVarsling,
			final AuthenticationLevel authenticationLevel, final Link... links) {
		this(messageId, subject, smsVarsling, authenticationLevel, links);
		personalIdentificationNumbers = new ArrayList<String>();
		personalIdentificationNumbers.add(id.asString());
	}

	public Message(final String messageId, final String subject, final DigipostAddress digipostAdress, final boolean smsVarsling,
			final AuthenticationLevel authenticationLevel, final Link... links) {
		this(messageId, subject, smsVarsling, authenticationLevel, links);
		recipients = new ArrayList<Recipient>();
		recipients.add(new Recipient(null, null, null, digipostAdress.asString(), null));
	}

	private Message(final String messageId, final String subject, final boolean smsVarsling, final AuthenticationLevel authenticationLevel,
			final Link... links) {
		super(links);
		this.messageId = messageId;
		this.subject = subject;
		smsNotification = smsVarsling;
		this.authenticationLevel = authenticationLevel;
	}

	public Link getSelfLink() {
		return getLinkByRelationName(Relation.SELF);
	}

	public Link getFileLink() {
		return getLinkByRelationName(Relation.ADD_CONTENT_AND_SEND);
	}

	public String getSubject() {
		return subject;
	}

	public MessageStatus getStatus() {
		return status;
	}

	public boolean hasSubject() {
		return !StringUtils.isBlank(subject);
	}

	public String getMessageId() {
		return messageId;
	}

	public boolean isSmsNotification() {
		return smsNotification;
	}

	public boolean isIdenticalTo(final Message message) {
		return messageId.equals(message.getMessageId()) && subject.equals(message.getSubject());
	}

	public AuthenticationLevel getAuthenticationLevel() {
		return authenticationLevel;
	}

	public void setStatus(final MessageStatus status) {
		this.status = status;
	}

	public void setPreEncrypt(final boolean preEncrypt) {
		this.preEncrypt = preEncrypt;
	}

	public void setSenderId(final long senderId) {
		this.senderId = senderId;
	}

	public boolean skalPrekrypteres() {
		return preEncrypt != null && preEncrypt;
	}

	public Link getEncryptionKeyLink() {
		return getLinkByRelationName(Relation.GET_ENCRYPTION_KEY);
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
}
