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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document-status", propOrder = {
        "attachments",
        "links"})
@XmlRootElement(name = "document-status")
public class DocumentStatus {

    @XmlAttribute(name = "uuid")
    public String uuid;
    @XmlAttribute(name = "status")
    public DeliveryStatus status;

    @XmlAttribute(name = "created")
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    public ZonedDateTime created;

    @XmlAttribute(name = "delivered", required = false)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    public ZonedDateTime delivered;

    @XmlAttribute(name = "read")
    public Read read;

    @XmlAttribute(name = "channel")
    public Channel channel;
    @XmlAttribute(name = "is-primary-document")
    public boolean isPrimaryDocument; //
    @XmlAttribute(name = "content-hash")
    public String contentHash;
    @XmlAttribute(name = "content-hash-algorithm")
    public HashAlgorithm contentHashAlgorithm;

    @XmlElement(name = "attachments")
    private List<DocumentStatus> attachments = new ArrayList<>();

    @XmlElement(name = "link")
    public List<Link> links = new ArrayList<>();

    public DocumentStatus() {
        this(null, null, null, null, null, null, true, null, null, null, null);
    }

    public DocumentStatus(String uuid, DeliveryStatus status, ZonedDateTime created, ZonedDateTime delivered, Read read, Channel channel,
                          boolean isPrimaryDocument, String contentHash, HashAlgorithm contentHashAlgorithm,
                          List<DocumentStatus> attachments, List<Link> links) {
        this.uuid = uuid;
        this.status = status;
        this.created = created;
        this.delivered = delivered;
        this.read = read;
        this.channel = channel;
        this.isPrimaryDocument = isPrimaryDocument;
        this.contentHash = contentHash;
        this.contentHashAlgorithm = contentHashAlgorithm;
        this.attachments = attachments;
        this.links = links;
    }

    public List<DocumentStatus> getAttachments() {
        return Collections.unmodifiableList(attachments == null ? new ArrayList<DocumentStatus>() : attachments);
    }
}
