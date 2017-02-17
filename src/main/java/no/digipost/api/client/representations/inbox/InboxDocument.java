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
package no.digipost.api.client.representations.inbox;

import no.digipost.api.client.representations.AuthenticationLevel;
import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;
import no.digipost.api.client.representations.xml.URIXmlAdapter;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inbox-document")
public class InboxDocument {

    @XmlElement()
    protected long id;
    @XmlElement(required = true)
    protected String subject;
    @XmlElement(required = true)
    protected String sender;
    @XmlElement(name = "delivery-time", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected DateTime deliveryTime;
    @XmlElement(name = "first-accessed", type = String.class)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected DateTime firstAccessed;
    @XmlElement(name = "authentication-level", required = true)
    @XmlSchemaType(name = "string")
    protected AuthenticationLevel authenticationLevel;
    @XmlElement(name = "content-type")
    protected String contentType;
    @XmlElement(name = "content-uri")
    @XmlJavaTypeAdapter(URIXmlAdapter.class)
    protected URI contentUri;
    @XmlElement(name = "delete-uri")
    @XmlJavaTypeAdapter(URIXmlAdapter.class)
    protected URI deleteUri;
    @XmlElement(name = "attachment")
    protected List<InboxDocument> attachments;

    public InboxDocument() {
    }

    public long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getSender() {
        return sender;
    }

    public DateTime getDeliveryTime() {
        return deliveryTime;
    }

    public DateTime getFirstAccessed() {
        return firstAccessed;
    }

    public AuthenticationLevel getAuthenticationLevel() {
        return authenticationLevel;
    }

    public String getContentType() {
        return contentType;
    }

    public URI getContentUri() {
        return contentUri;
    }

    public URI getDeleteUri() {
        return deleteUri;
    }

    public List<InboxDocument> getAttachments() {
        return attachments;
    }
}
