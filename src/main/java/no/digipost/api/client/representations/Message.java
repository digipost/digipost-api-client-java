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
import java.util.Comparator;
import java.util.List;

import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.Channel.PRINT;
import static no.motif.Singular.the;
import static org.apache.commons.lang3.ArrayUtils.INDEX_NOT_FOUND;
import static org.apache.commons.lang3.ArrayUtils.indexOf;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.join;

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
public class Message implements MayHaveSender {

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
			return recipient(new MessageRecipient(digipostAddress));
		}

		public MessageBuilder personalIdentificationNumber(PersonalIdentificationNumber personalIdentificationNumber) {
			return recipient(new MessageRecipient(personalIdentificationNumber));
		}

		public MessageBuilder organisationNumber(OrganisationNumber organisationNumber) {
			return recipient(new MessageRecipient(organisationNumber));
		}

		public MessageBuilder nameAndAddress(NameAndAddress nameAndAddress) {
			return recipient(new MessageRecipient(nameAndAddress));
		}

		public MessageBuilder printDetails(PrintDetails printDetails) {
			return recipient(new MessageRecipient(printDetails));
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
		this.deliveryTime = deliveryTime;
		this.attachments = new ArrayList<>();
		for (Document attachment : defaultIfNull(attachments, Collections.<Document>emptyList())) {
	        this.attachments.add(attachment);
        }
	}

	/**
	 * @return a list containing every {@link Document} in this delivery.
	 *         The primary document will be the first element of the list,
	 *         with the attachments following. The list is immutable and
	 *         can not be used to change which documents are in this
	 *         MessageDelivery.
	 */
	public List<Document> getAllDocuments() {
		return the(primaryDocument).append(attachments).collect();
	}

	public boolean isDirectPrint() {
		return recipient.isDirectPrint();
	}

	public boolean isSameMessageAs(final Message message) {
		return this.messageId != null && this.messageId.equals(message.messageId);
	}

	public boolean hasAnyDocumentRequiringPreEncryption() {
		return the(primaryDocument).append(attachments).exists(Document.isPreEncrypt);
	}

    public Channel getChannel() {
		return recipient.isDirectPrint() ? PRINT : DIGIPOST;
    }


    /**
     * @return {@link Comparator} which order documents by the same order as they are contained in
     *         this message. If a document
     */
	public Comparator<? super Document> documentOrder() {
		return new Comparator<Document>() {
			final String[] uuids = the(primaryDocument).append(attachments).map(Document.getUuid).collect().toArray(new String[attachments.size() + 1]);
			@Override
            public int compare(Document d1, Document d2) {
				int d1Index = indexOf(uuids, d1.uuid);
				if (d1Index == INDEX_NOT_FOUND) {
					throw new CannotSortDocumentsUsingMessageOrder(d1.uuid, uuids);
				}

				int d2Index = indexOf(uuids, d2.uuid);
				if (d2Index == INDEX_NOT_FOUND) {
					throw new CannotSortDocumentsUsingMessageOrder(d2.uuid, uuids);
				}
				return d1Index - d2Index;
            }};
    }

	public class CannotSortDocumentsUsingMessageOrder extends IllegalStateException {
		private CannotSortDocumentsUsingMessageOrder(String uuid, String[] validUuids) {
			super(
					"Kan ikke sortere Document med uuid '" + uuid + "' etter rekkefølgen i Message med id '" + messageId +
					"' da dokumentet ikke eksisterer i meldingen.\nMeldingen har følgende dokumenter:\n  - " +
					join(validUuids, "\n  - "));
		}
	}

	@Override
	public Long getSenderId() {
		return senderId;
	}

	@Override
	public SenderOrganization getSenderOrganization() {
		return senderOrganization;
	}

}
