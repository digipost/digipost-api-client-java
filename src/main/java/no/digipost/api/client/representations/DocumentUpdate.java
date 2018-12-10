package no.digipost.api.client.representations;

import no.digipost.api.datatypes.DataType;

import javax.xml.bind.annotation.*;
import java.util.regex.PatternSyntaxException;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document-update", propOrder = {
        "dataType",
        "senderOrganization",
        "senderId",
        "smsNotification",
        "emailNotification"
})
@XmlRootElement(name = "document-update")
public class DocumentUpdate {

    @XmlElement(name = "data-type", required = true)
    protected DataType dataType;
    @XmlElement(name = "sender-organization", nillable = false)
    protected SenderOrganization senderOrganization;
    @XmlElement(name = "sender-id", nillable = false)
    protected Long senderId;
    @XmlElement(name = "sms-notification", nillable = false)
    protected SmsNotification smsNotification;
    @XmlElement(name = "email-notification", nillable = false)
    protected EmailNotification emailNotification;
    @XmlAttribute(name = "notify-user")
    protected Boolean notifyUser;

    DocumentUpdate() {
        this(null, null, null, null, null, null);
    }

    public static class DocumentUpdateBuilder {
        private DataType dataType = null;
        private SenderOrganization senderOrganization = null;
        private Long senderId = null;
        private SmsNotification smsNotification = null;
        private EmailNotification emailNotification = null;
        private Boolean notifyUser = null;

        private DocumentUpdateBuilder(DataType dataType) {
            this.dataType = dataType;
        }

        public static DocumentUpdateBuilder newDocumentUpdate(DataType dataType) {
            return new DocumentUpdateBuilder(dataType);
        }

        public DocumentUpdateBuilder setSenderOrganization(SenderOrganization senderOrganization) {
            this.senderOrganization = senderOrganization;
            return this;
        }

        public DocumentUpdateBuilder setSenderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public DocumentUpdateBuilder setSmsNotification(SmsNotification smsNotification) {
            this.smsNotification = smsNotification;
            return this;
        }

        public DocumentUpdateBuilder setEmailNotification(EmailNotification emailNotification) {
            this.emailNotification = emailNotification;
            return this;
        }

        public DocumentUpdateBuilder setNotifyUser(Boolean notifyUser) {
            this.notifyUser = notifyUser;
            return this;
        }

        public DocumentUpdate build() {
            if (senderId != null && senderOrganization != null) {
                throw new IllegalStateException("You can't set both senderId *and* senderOrganization.");
            }
            return new DocumentUpdate(dataType, senderOrganization, senderId, smsNotification, emailNotification, notifyUser);
        }
    }

    private DocumentUpdate(DataType dataType, SenderOrganization senderOrganization, Long senderId, SmsNotification smsNotification, EmailNotification emailNotification, Boolean notifyUser) {
        this.dataType = dataType;
        this.senderOrganization = senderOrganization;
        this.senderId = senderId;
        this.smsNotification = smsNotification;
        this.emailNotification = emailNotification;
        this.notifyUser = notifyUser;
    }
}
