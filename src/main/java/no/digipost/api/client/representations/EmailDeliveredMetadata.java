package no.digipost.api.client.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "email-delivered-metadata")
public class EmailDeliveredMetadata extends EventMetadata {

    @XmlAttribute(name = "email-address")
    public final String emailAddress;

    public EmailDeliveredMetadata() { this(null); }

    public EmailDeliveredMetadata(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
