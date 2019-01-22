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

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-delivery", propOrder = {
        "messageId",
        "deliveryMethod",
        "senderId",
        "status",
        "deliveryTime",
        "primaryDocument",
        "attachments",
        "links"
    })
@XmlRootElement(name = "message-delivery")
public class MessageDelivery extends Representation implements MayHaveSender {

    @XmlElement(name = "message-id")
    protected String messageId;
    @XmlElement(name = "delivery-method", required = true)
    protected Channel deliveryMethod;
    @XmlElement(name = "sender-id", nillable = false)
    protected long senderId;
    @XmlElement(required = true)
    protected MessageStatus status;
    @XmlElement(name = "delivery-time", type = String.class, nillable = false)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected ZonedDateTime deliveryTime;
    @XmlElement(name = "primary-document", required = true)
    protected Document primaryDocument;
    @XmlElement(name = "attachment")
    protected List<Document> attachments;

    public MessageDelivery() {
    }

    public MessageDelivery(String messageId, Channel channel, MessageStatus status, ZonedDateTime deliveryTime) {
        this.messageId = messageId;
        this.deliveryMethod = channel;
        this.status = status;
        this.deliveryTime = deliveryTime;
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
        return messageId.equals(message.messageId);
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
        return Channel.DIGIPOST.equals(deliveryMethod);
    }

    public boolean isAlreadyDeliveredToDigipost() {
        return Channel.DIGIPOST.equals(deliveryMethod) && deliveryTime != null;
    }

    public Link getSelfLink() {
        return getLinkByRelationName(Relation.SELF);
    }

    public Channel getChannel() {
        return deliveryMethod;
    }

    public ZonedDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public Document getPrimaryDocument() {
        return primaryDocument;
    }

    /**
     * @return an ordered Stream containing every {@link Document} in this delivery.
     *         The primary document will be the first element of the stream,
     *         with the attachments following.
     */
    public Stream<Document> getAllDocuments() {
        return Stream.concat(ofNullable(primaryDocument).map(Stream::of).orElseGet(Stream::empty), getAttachments().stream());
    }

    public Document getDocument(UUID uuid) {
        return getAllDocuments().filter(doc -> Objects.equals(uuid, doc.uuid)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Document with UUID '" + uuid + "' was not found in this " + getClass().getSimpleName() + "."));
    }

    /**
     * Always returns the resolved sender-id of the message, i.e. what the receiver
     * of the message sees as the sender of the message. If the originating {@link Message} has specified
     * no {@link Message#senderId sender-id} nor {@link Message#senderOrganization sender-organization},
     * it will be set to the broker-id which was specified in the X-Digipost-UserId header of the initiating request.
     *
     * @return always the sender-id, never {@code null}.
     */
    @Override
    public Optional<SenderId> getSenderId() {
        return Optional.of(senderId).map(SenderId::of);
    }

    /**
     * @return always {@link Optional#empty()}
     * @see MessageDelivery#getSenderId()
     */
    @Override
    public Optional<SenderOrganization> getSenderOrganization() {
        return Optional.empty();
    }
}
