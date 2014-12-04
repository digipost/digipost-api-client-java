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
@XmlType(name = "message", propOrder = {
		"messageId",
		"senderId",
		"senderOrganization",
		"recipient",
		"deliveryTime",
		"invoiceReference",
		"primaryDocument",
		"attachments" })
@XmlRootElement(name = "message")
public class Message {

	@XmlElement(name = "message-id")
	public final String messageId;
	@XmlElement(name = "sender-id")
	public final Long senderId;
	@XmlElement(name = "sender-organization")
	public final SenderOrganization senderOrganization;
	@XmlElement(name = "recipient")
	public final MessageRecipient recipient;
	@XmlElement(name = "delivery-time", type = String.class, nillable = false)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	public final DateTime deliveryTime;
	@XmlElement(name = "invoice-reference")
	public final String invoiceReference;
	@XmlElement(name = "primary-document", required = true)
	public final Document primaryDocument;
	@XmlElement(name = "attachment")
	public final List<Document> attachments;

	Message() {
		this(null, null, null, null, null, null, null, null);
	}

	public static class MessageBuilder {
		private String messageId;
		private Long senderId;
		private SenderOrganization senderOrganization;
		private MessageRecipient recipient;
		private DateTime deliveryTime;
		private Document primaryDocument;
		private List<Document> attachments = new ArrayList<>();
		private String invoiceReference;

		private MessageBuilder(String messageId, Document primaryDocument) {
			this.messageId = messageId;
			this.primaryDocument = primaryDocument;
		}

		public static MessageBuilder newMessage(String messageId, Document primaryDocument) {
			return new MessageBuilder(messageId, primaryDocument);
		}

		/**
		 * Only neccessary when sending on behalf of another user. In this case
		 * senderId must be the party you are sending on behalf of. Your own user id
		 * should be set in the http header X-Digipost-UserId.
		 */
		public MessageBuilder senderId(Long senderId) {
			this.senderId = senderId;
			return this;
		}

		/**
		 * Only neccessary when sending on behalf of another user. In this case
		 * senderOrganization must be the party you are sending on behalf of.
		 * Your own user id should be set in the http header X-Digipost-UserId.
		 */
		public MessageBuilder senderOrganization(SenderOrganization senderOrganization) {
			this.senderOrganization = senderOrganization;
			return this;
		}

		public MessageBuilder recipient(MessageRecipient recipient) {
			this.recipient = recipient;
			return this;
		}

		public MessageBuilder digipostAddress(DigipostAddress digipostAddress) {
			this.recipient = new MessageRecipient(digipostAddress);
			return this;
		}

		public MessageBuilder personalIdentificationNumber(PersonalIdentificationNumber personalIdentificationNumber) {
			this.recipient = new MessageRecipient(personalIdentificationNumber);
			return this;
		}

		public MessageBuilder organisationNumber(OrganisationNumber organisationNumber) {
			this.recipient = new MessageRecipient(organisationNumber);
			return this;
		}

		public MessageBuilder nameAndAddress(NameAndAddress nameAndAddress) {
			this.recipient = new MessageRecipient(nameAndAddress);
			return this;
		}

		public MessageBuilder deliveryTime(DateTime deliveryTime) {
			this.deliveryTime = deliveryTime;
			return this;
		}

		public MessageBuilder invoiceReference(String invoiceReference) {
			this.invoiceReference = invoiceReference;
			return this;
		}

		public MessageBuilder attachments(Iterable<? extends Document> attachments) {
			for (Document attachment : defaultIfNull(attachments, Collections.<Document>emptyList())) {
				this.attachments.add(attachment);
			}
			return this;
		}

		public Message build() {
			if (recipient == null) {
				throw new IllegalStateException("You must specify a recipient.");
			}
			if (senderId != null && senderOrganization != null) {
				throw new IllegalStateException("You can't set both senderId *and* senderOrganization.");
			}
			return new Message(messageId, senderId, senderOrganization, recipient, primaryDocument, attachments,
					deliveryTime, invoiceReference);
		}

	}

	private Message(String messageId, Long senderId, SenderOrganization senderOrganization, MessageRecipient recipient,
	                Document primaryDocument, Iterable<? extends Document> attachments, DateTime deliveryTime,
					String invoiceReference) {
		this.messageId = messageId;
		this.senderId = senderId;
		this.senderOrganization = senderOrganization;
		this.recipient = recipient;
		this.primaryDocument = primaryDocument;
		this.invoiceReference = invoiceReference;
		this.attachments = new ArrayList<>();
		this.deliveryTime = deliveryTime;
		for (Document attachment : defaultIfNull(attachments, Collections.<Document>emptyList())) {
	        this.attachments.add(attachment);
        }
	}

	public boolean isDirectPrint() {
		return recipient.isDirectPrint();
	}

	public boolean isSameMessageAs(final Message message) {
		return this.messageId != null && this.messageId.equals(message.messageId);
	}
}
