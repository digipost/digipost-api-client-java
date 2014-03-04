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

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message", propOrder = { "messageId", "senderId", "recipient", "primaryDocument", "attachments" })
@XmlRootElement(name = "message")
public class Message {

	@XmlElement(name = "message-id")
	protected String messageId;
	@XmlElement(name = "sender-id")
	protected Long senderId;
	@XmlElement(name = "recipient")
	protected MessageRecipient recipient;
	@XmlElement(name = "primary-document", required = true)
	protected Document primaryDocument;
	@XmlElement(name = "attachment")
	protected List<Document> attachments;

	Message() {
	}

	public Message(String messageId, PersonalIdentificationNumber id, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(id), primaryDocument, attachments);
	}

	public Message(String messageId, OrganisationNumber id, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(id), primaryDocument, attachments);
	}

	public Message(String messageId, DigipostAddress digipostAdress, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(digipostAdress), primaryDocument, attachments);
	}

	public Message(String messageId, NameAndAddress nameAndAddress, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(nameAndAddress), primaryDocument, attachments);
	}

	public Message(String messageId, MessageRecipient recipient, Document primaryDocument, Iterable<? extends Document> attachments) {
		this.messageId = messageId;
		this.recipient = recipient;
		this.primaryDocument = primaryDocument;
		this.attachments = new ArrayList<Document>();
		for (Document attachment : defaultIfNull(attachments, Collections.<Document>emptyList())) {
	        this.attachments.add(attachment);
        }
	}

	public MessageRecipient getRecipient() {
		return recipient;
	}

	/**
	 * Only neccessary when sending on behalf of another user. In this case
	 * senderId must be the party you are sending on behalf of. Your own user id
	 * should be set in the http header X-Digipost-UserId.
	 */
	public void setSenderId(final long senderId) {
		this.senderId = senderId;
	}

	public boolean isDirectPrint() {
		return recipient.isDirectPrint();
	}

	public String getMessageId() {
		return messageId;
	}

	public Long getSenderId() {
		return senderId;
	}

	public Document getPrimaryDocument() {
		return primaryDocument;
	}

	public List<Document> getAttachments() {
		return attachments;
	}

	public boolean isSameMessageAs(final Message message) {
		return this.messageId != null && this.messageId.equals(message.messageId);
	}
}
