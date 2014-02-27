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

import static java.util.Collections.unmodifiableList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import org.joda.time.DateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-delivery", propOrder = { "messageId", "deliveryMethod", "status", "deliveredDate", "primaryDocument",
		"attachments", "links" })
@XmlRootElement(name = "message-delivery")
public class MessageDelivery extends Representation {

	@XmlElement(name = "message-id")
	protected String messageId;
	@XmlElement(name = "delivery-method", required = true)
	protected DeliveryMethod deliveryMethod;
	@XmlElement(required = true)
	protected MessageStatus status;
	@XmlElement(name = "delivered-date", type = String.class, nillable = false)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	protected DateTime deliveredDate;
	@XmlElement(name = "primary-document", required = true)
	protected Document primaryDocument;
	@XmlElement(name = "attachment")
	protected List<Document> attachments;

	public MessageDelivery() {
	}

	public MessageDelivery(String messageId, DeliveryMethod deliveryMethod, MessageStatus status, DateTime deliveredDate) {
		this.messageId = messageId;
		this.deliveryMethod = deliveryMethod;
		this.status = status;
		this.deliveredDate = deliveredDate;
	}

	@XmlElement(name = "link")
	protected List<Link> getLinks() {
		return links;
	}

	protected void setLinks(final List<Link> links) {
		this.links = links;
	}

	public String getMessageId() {
		return messageId;
	}

	public List<Document> getAttachments() {
		return attachments != null ? unmodifiableList(attachments) : Collections.<Document>emptyList();
	}

	public boolean isSameMessageAs(final Message message) {
		return messageId.equals(message.getMessageId());
	}

	public MessageStatus getStatus() {
		return status;
	}

	public Link getEncryptionKeyLink() {
		return getLinkByRelationName(Relation.GET_ENCRYPTION_KEY);
	}

	public Link getSendLink() {
		return getLinkByRelationName(Relation.SEND);
	}

	public boolean willBeDeliveredInDigipost() {
		return DeliveryMethod.DIGIPOST.equals(deliveryMethod);
	}

	public boolean isAlreadyDeliveredToDigipost() {
		return DeliveryMethod.DIGIPOST.equals(deliveryMethod) && deliveredDate != null;
	}

	public Link getSelfLink() {
		return getLinkByRelationName(Relation.SELF);
	}

	public DeliveryMethod getDeliveryMethod() {
		return deliveryMethod;
	}

	public DateTime getDeliveredDate() {
		return deliveredDate;
	}

	public Document getPrimaryDocument() {
		return primaryDocument;
	}

	/**
	 * @return a list containing every {@link Document} in this delivery.
	 *         The primary document will be the first element of the list,
	 *         with the attachments following. The list is immutable and
	 *         can not be used to change which documents are in this
	 *         MessageDelivery.
	 */
	public List<Document> getAllDocuments() {
		LinkedList<Document> all = new LinkedList<Document>(getAttachments());
		if (primaryDocument != null) {
			all.addFirst(primaryDocument);
		}
		return unmodifiableList(all);
	}

	public Document getDocumentByUuid(String uuid) {
		for (Document document : getAllDocuments()) {
			if (uuid.equals(document.getUuid())) return document;
		}
		throw new IllegalArgumentException("Document with UUID '" + uuid + "' was not found in this " + getClass().getSimpleName() + ".");
    }
}
