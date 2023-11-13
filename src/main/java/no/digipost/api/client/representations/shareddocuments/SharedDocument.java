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
package no.digipost.api.client.representations.shareddocuments;

import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;
import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import static no.digipost.api.client.representations.Relation.GET_SHARED_DOCUMENT_CONTENT;
import static no.digipost.api.client.representations.Relation.GET_SHARED_DOCUMENT_CONTENT_STREAM;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "shared-document", propOrder = {
        "deliveryTime",
        "subject",
        "fileType",
        "fileSizeBytes",
        "origin",
        "links"
})
public class SharedDocument extends Representation {

    @XmlElement(name = "delivery-time", required = true)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    private ZonedDateTime deliveryTime;
    @XmlElement(name = "subject", required = true)
    private String subject;
    @XmlElement(name = "file-type", required = true)
    private String fileType;
    @XmlElement(name = "origin", required = true)
    private SharedDocumentOrigin origin;
    @XmlElement(name = "file-size-bytes", required = true)
    private Integer fileSizeBytes;

    public SharedDocument() {
    }

    public SharedDocument(ZonedDateTime deliveryTime, String subject, String fileType, SharedDocumentOrigin origin, Integer fileSizeBytes, List<Link> links) {
        this.deliveryTime = deliveryTime;
        this.subject = subject;
        this.fileType = fileType;
        this.origin = origin;
        this.fileSizeBytes = fileSizeBytes;
        this.links = links;
    }

    public ZonedDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(ZonedDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public SharedDocumentOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(SharedDocumentOrigin origin) {
        this.origin = origin;
    }

    public Integer getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Integer fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        return this.links;
    }

    protected void setLink(final List<Link> links) {
        this.links = links;
    }

    public URI getSharedDocumentContent() {
        return getLinkByRelationName(GET_SHARED_DOCUMENT_CONTENT).getUri();
    }

    public URI getSharedDocumentContentStream() {
        return getLinkByRelationName(GET_SHARED_DOCUMENT_CONTENT_STREAM).getUri();
    }


    @Override
    public String toString() {
        return "SharedDocument{" +
                "deliveryTime=" + deliveryTime +
                ", subject='" + subject + '\'' +
                ", fileType='" + fileType + '\'' +
                ", origin=" + origin +
                ", fileSizeBytes=" + fileSizeBytes +
                ", links=" + links +
                '}';
    }
}
