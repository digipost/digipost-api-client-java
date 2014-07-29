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

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message", propOrder = { "messageId", "senderId", "senderOrganization", "recipient", "primaryDocument", "attachments", "receivedDate" })
@XmlRootElement(name = "message")
public class Message {

	@XmlElement(name = "message-id")
	protected String messageId;
	@XmlElement(name = "sender-id")
	protected Long senderId;
	@XmlElement(name = "sender-organization")
	protected SenderOrganization senderOrganization;
	@XmlElement(name = "recipient")
	protected MessageRecipient recipient;
	@XmlElement(name = "primary-document", required = true)
	protected Document primaryDocument;
	@XmlElement(name = "attachment")
	protected List<Document> attachments;
	@XmlElement(name = "received-date", type = String.class, nillable = false)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	protected DateTime receivedDate;

	public Message() {
	}

	public Message(String messageId, PersonalIdentificationNumber id, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(id), primaryDocument, attachments, null);
	}

	public Message(String messageId, OrganisationNumber id, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(id), primaryDocument, attachments, null);
	}

	public Message(String messageId, DigipostAddress digipostAdress, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(digipostAdress), primaryDocument, attachments, null);
	}

	public Message(String messageId, NameAndAddress nameAndAddress, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, new MessageRecipient(nameAndAddress), primaryDocument, attachments, null);
	}

	public Message(String messageId, NameAndAddress nameAndAddress, Document primaryDocument, Iterable<? extends Document> attachments, DateTime receivedDate) {
		this(messageId, new MessageRecipient(nameAndAddress), primaryDocument, attachments, receivedDate);
	}

	public Message(String messageId, MessageRecipient recipient, Document primaryDocument, Iterable<? extends Document> attachments) {
		this(messageId, recipient, primaryDocument, attachments, null);
	}

	public Message(String messageId, MessageRecipient recipient, Document primaryDocument, Iterable<? extends Document> attachments, DateTime receivedDate) {
		this.messageId = messageId;
		this.recipient = recipient;
		this.primaryDocument = primaryDocument;
		this.attachments = new ArrayList<>();
		this.receivedDate = receivedDate;
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

	public void setSenderOrganization(final SenderOrganization senderOrganization) {
		this.senderOrganization = senderOrganization;
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

	public SenderOrganization getSenderOrganization() {
		return senderOrganization;
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

	public DateTime getReceivedDate() {
		return receivedDate;
	}
}
