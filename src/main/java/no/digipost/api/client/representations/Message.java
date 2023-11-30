/*
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
import no.digipost.api.client.representations.batch.Batch;
import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.concat;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.Channel.PRINT;
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
        "attachments",
        "printIfUnread", 
        "requestForRegistration",
        "batch"})
@XmlRootElement(name = "message")
public class Message implements MayHaveSender {

    public static MessageBuilder newMessage(UUID messageId, Document primaryDocument) {
        return newMessage(messageId.toString(), primaryDocument);
    }

    public static MessageBuilder newMessage(String messageId, Document primaryDocument) {
        return new MessageBuilder(messageId, primaryDocument);
    }


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
    public final ZonedDateTime deliveryTime;
    @XmlElement(name = "invoice-reference")
    public final String invoiceReference;
    @XmlElement(name = "primary-document", required = true)
    public final Document primaryDocument;
    @XmlElement(name = "attachment")
    public final List<Document> attachments;
    @XmlElement(name = "print-if-unread")
    public final PrintIfUnread printIfUnread;
    @XmlElement(name = "request-for-registration")
    public final RequestForRegistration requestForRegistration;
    @XmlElement(name="batch")
    public final Batch batch;

    Message() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public static class MessageBuilder {
        private String messageId;
        private Long senderId;
        private SenderOrganization senderOrganization;
        private MessageRecipient recipient;
        private ZonedDateTime deliveryTime;
        private Document primaryDocument;
        private final List<Document> attachments = new ArrayList<>();
        private String invoiceReference;
        private PrintIfUnread printIfUnread;
        private RequestForRegistration requestForRegistration;
        private Batch batch;

        private MessageBuilder(String messageId, Document primaryDocument) {
            this.messageId = messageId;
            this.primaryDocument = primaryDocument;
        }

        /**
         * Only neccessary when sending on behalf of another user. In this case
         * senderId must be the party you are sending on behalf of. Your own user id
         * should be set in the http header X-Digipost-UserId.
         */
        public MessageBuilder senderId(SenderId senderId) {
            this.senderId = senderId.value();
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

        public MessageBuilder recipient(DigipostAddress digipostAddress) {
            return recipient(new MessageRecipient(digipostAddress));
        }

        public MessageBuilder recipient(PersonalIdentificationNumber personalIdentificationNumber) {
            return recipient(new MessageRecipient(personalIdentificationNumber));
        }

        public MessageBuilder recipient(BankAccountNumber bankAccountNumber) {
            return recipient(new MessageRecipient(bankAccountNumber));
        }

        public MessageBuilder recipient(OrganisationNumber organisationNumber) {
            return recipient(new MessageRecipient(organisationNumber));
        }

        public MessageBuilder recipient(NameAndAddress nameAndAddress) {
            return recipient(new MessageRecipient(nameAndAddress));
        }

        public MessageBuilder recipient(PeppolAddresses peppolAddresses) {
            return recipient(new MessageRecipient(peppolAddresses));
        }

        public MessageBuilder printDetails(PrintDetails printDetails) {
            return recipient(new MessageRecipient(printDetails));
        }

        public MessageBuilder deliveryTime(ZonedDateTime deliveryTime) {
            this.deliveryTime = deliveryTime;
            return this;
        }

        public MessageBuilder invoiceReference(String invoiceReference) {
            this.invoiceReference = invoiceReference;
            return this;
        }

        public MessageBuilder printIfUnread(PrintIfUnread printIfUnread) {
            this.printIfUnread = printIfUnread;
            return this;
        }

        public MessageBuilder requestForRegistration(RequestForRegistration requestForRegistration) {
            this.requestForRegistration = requestForRegistration;
            return this;
        }

        public MessageBuilder attachments(Document ... attachments) {
            return attachments(asList(attachments));
        }

        public MessageBuilder attachments(Iterable<? extends Document> attachments) {
            defaultIfNull(attachments, Collections.<Document>emptyList()).forEach(this.attachments::add);
            return this;
        }

        public MessageBuilder batch(UUID batchUUID) {
            this.batch = new Batch(batchUUID.toString());
            return this;
        }
        
        public Message build() {
            if (recipient == null) {
                throw new IllegalStateException("You must specify a recipient.");
            }
            if (senderId != null && senderOrganization != null) {
                throw new IllegalStateException("You can't set both senderId *and* senderOrganization.");
            }
            return new Message(messageId, senderId, senderOrganization, recipient, primaryDocument, attachments, deliveryTime, invoiceReference, printIfUnread, requestForRegistration, batch);
        }

    }

    private Message(String messageId, Long senderId, SenderOrganization senderOrganization, MessageRecipient recipient,
                    Document primaryDocument, Iterable<? extends Document> attachments, ZonedDateTime deliveryTime,
                    String invoiceReference, PrintIfUnread printIfUnread, RequestForRegistration requestForRegistration, Batch batch) {
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
        this.printIfUnread = printIfUnread;
        this.requestForRegistration = requestForRegistration;
        this.batch = batch;
    }

    /**
     * Copies a message and forces all its documents to have file type of PDF.
     *
     * @param messageToCopy message to copy
     * @return a copy of <code>messageToCopy</code>
     */
    public static Message copyPrintMessage(Message messageToCopy){
        List<Document> tmpAttachments = new ArrayList<>();
        for(Document attachment : messageToCopy.attachments){
            tmpAttachments.add(attachment.copyDocumentAndSetDigipostFileTypeToPdf());
        }
        return copyMessage(
                messageToCopy, messageToCopy.primaryDocument.copyDocumentAndSetDigipostFileTypeToPdf(), tmpAttachments,
                messageToCopy.recipient.getPrintDetails()
        );
    }

    public static Message copyMessageWithOnlyDigipostDetails(Message messageToCopy){
        return copyMessage(messageToCopy, messageToCopy.primaryDocument, messageToCopy.attachments, null);
    }

    private static Message copyMessage(Message messageToCopy, Document newPrimaryDocument, List<Document> newAttachments, PrintDetails newPrintDetails) {
        return new Message(
                messageToCopy.messageId, messageToCopy.senderId, messageToCopy.senderOrganization,
                messageToCopy.recipient.nameAndAddress, messageToCopy.recipient.digipostAddress,
                messageToCopy.recipient.personalIdentificationNumber, messageToCopy.recipient.organisationNumber,
                messageToCopy.deliveryTime, messageToCopy.invoiceReference,
                newPrimaryDocument, newAttachments,
                newPrintDetails, messageToCopy.recipient.bankAccountNumber,
                messageToCopy.printIfUnread, messageToCopy.requestForRegistration,
                messageToCopy.recipient.peppolAddresses, messageToCopy.recipient.emailDetails, messageToCopy.batch
        );
    }

    private Message(final String messageId, final Long senderId, final SenderOrganization senderOrganization,
                    final NameAndAddress nameAndAddress, final String digipostAddress, String personalIdentificationNumber,
                    final String organisationNumber, final ZonedDateTime deliveryTime, final String invoiceReference,
                    final Document primaryDocument, final List<Document> attachments, final PrintDetails printDetails, final String bankAccountNumber,
                    final PrintIfUnread printIfUnread, final RequestForRegistration requestForRegistration, final PeppolAddresses peppolAddresses, final EmailDetails emailDetails,
                    final Batch batch
    ){
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderOrganization = senderOrganization;
        MessageRecipient recipient = new MessageRecipient(nameAndAddress, digipostAddress,
                peppolAddresses, personalIdentificationNumber, organisationNumber, printDetails, bankAccountNumber, emailDetails);
        this.recipient = recipient;
        this.deliveryTime = deliveryTime;
        this.invoiceReference = invoiceReference;
        this.primaryDocument = primaryDocument;
        this.attachments = attachments;
        this.printIfUnread = printIfUnread;
        this.requestForRegistration = requestForRegistration;
        this.batch = batch;
    }


    /**
     * @return an ordered Stream containing every {@link Document} in this message.
     *         The primary document will be the first element of the list,
     *         with the attachments following.
     */
    public Stream<Document> getAllDocuments() {
        return concat(ofNullable(primaryDocument).map(Stream::of).orElseGet(Stream::empty), attachments.stream());
    }

    public boolean isDirectPrint() {
        return recipient.isDirectPrint();
    }

    public boolean isSameMessageAs(final Message message) {
        return this.messageId != null && this.messageId.equals(message.messageId);
    }

    public boolean hasAnyDocumentRequiringEncryption() {
        return getAllDocuments().anyMatch(Document::willBeEncrypted);
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
            final UUID[] uuids = getAllDocuments().map(d -> d.uuid).toArray(UUID[]::new);
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
        private CannotSortDocumentsUsingMessageOrder(UUID uuid, UUID[] validUuids) {
            super(
                    "Kan ikke sortere Document med uuid '" + uuid + "' etter rekkefølgen i Message med id '" + messageId +
                    "' da dokumentet ikke eksisterer i meldingen.\nMeldingen har følgende dokumenter:\n  - " +
                    join(validUuids, "\n  - "));
        }
    }

    @Override
    public Optional<SenderId> getSenderId() {
        return Optional.ofNullable(senderId).map(SenderId::of);
    }

    @Override
    public Optional<SenderOrganization> getSenderOrganization() {
        return Optional.ofNullable(senderOrganization);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", senderId=" + senderId +
                ", senderOrganization=" + senderOrganization +
                ", recipient=" + recipient +
                ", deliveryTime=" + deliveryTime +
                ", invoiceReference='" + invoiceReference + '\'' +
                ", primaryDocument=" + primaryDocument +
                ", attachments=" + attachments +
                '}';
    }

}
