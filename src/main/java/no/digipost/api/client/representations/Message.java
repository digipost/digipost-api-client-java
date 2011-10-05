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

import static no.digipost.api.client.representations.MessageStatus.EXPECTING_CONTENT;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "messageId", "subject", "digipostAddress", "personalIdentificationNumber", "smsNotification", "status", "link",
		"authenticationLevel" })
@XmlRootElement
public class Message extends Representation {
	@XmlElement(required = true)
	private String messageId;
	@XmlElement(required = true)
	private String subject;
	@XmlTransient
	private RecipientIdentifier recipientIdentifier;
	private boolean smsNotification;
	private MessageStatus status;
	private AuthenticationLevel authenticationLevel;

	public Message(final String messageId, final String subject, final RecipientIdentifier recipientIdentifier,
			final boolean smsNotification, final AuthenticationLevel authenticationLevel, final Link... links) {
		this(messageId, subject, recipientIdentifier, smsNotification, EXPECTING_CONTENT, authenticationLevel, links);
	}

	public Message(final String messageId, final String subject, final RecipientIdentifier recipientIdentifier,
			final boolean smsNotification, final MessageStatus status, final AuthenticationLevel authenticationLevel, final Link... links) {
		super(links);
		this.messageId = messageId;
		this.subject = subject;
		this.recipientIdentifier = recipientIdentifier;
		this.smsNotification = smsNotification;
		this.status = status;
		this.authenticationLevel = authenticationLevel;
	}

	Message() {
	}

	public Link getSelfLink() {
		return getLinkByRelationName(Relation.SELF);
	}

	public Link getFileLink() {
		return getLinkByRelationName(Relation.ADD_CONTENT);
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

	/**
	 * To forsendelser regnes som identiske hvis de har samme forsendelses-id og
	 * emne og mottakerne har samme kunde-id.
	 */
	public boolean isIdenticalTo(final Message message) {
		return messageId.equals(message.getMessageId()) && subject.equals(message.getSubject());
	}

	public RecipientIdentifier getRecipientIdentifier() {
		return recipientIdentifier;
	}

	public AuthenticationLevel getAuthenticationLevel() {
		return authenticationLevel;
	}

	@XmlElement
	protected String getDigipostAddress() {
		if (recipientIdentifier.isPersonalIdentificationNumber()) {
			return null;
		} else {
			return recipientIdentifier.asString();
		}
	}

	protected void setDigipostAddress(final String digipostAddress) {
		recipientIdentifier = new DigipostAddress(digipostAddress);
	}

	@XmlElement
	protected String getPersonalIdentificationNumber() {
		if (recipientIdentifier.isPersonalIdentificationNumber()) {
			return recipientIdentifier.asString();
		} else {
			return null;
		}
	}

	protected void setPersonalIdentificationNumber(final String personalIdentificationNumber) {
		recipientIdentifier = new PersonalIdentificationNumber(personalIdentificationNumber);
	}

	public void setStatus(final MessageStatus status) {
		this.status = status;
	}

	@XmlElement(name = "link")
	protected List<Link> getLink() {
		return links;
	}

	protected void setLink(final List<Link> links) {
		this.links = links;
	}
}
