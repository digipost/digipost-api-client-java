/*
 * Copyright (C) Posten Bring AS
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static no.digipost.api.client.representations.Relation.STOP_SHARING;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "share-documents-request-state", propOrder = {
        "sharedDocuments",
        "sharedAtTime",
        "expiryTime",
        "withdrawnTime",
        "links"
})
@XmlRootElement(name = "share-documents-request-state")
public class ShareDocumentsRequestState extends Representation {

    @XmlElementWrapper(name = "shared-documents")
    @XmlElement(name = "shared-document", required = true)
    private List<SharedDocument> sharedDocuments = new ArrayList<>();
    @XmlElement(name = "shared-at-time")
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    private ZonedDateTime sharedAtTime;
    @XmlElement(name = "expiry-time")
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    private ZonedDateTime expiryTime;
    @XmlElement(name = "withdrawn-time")
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    private ZonedDateTime withdrawnTime;

    public ShareDocumentsRequestState() {
    }

    public ShareDocumentsRequestState(List<SharedDocument> sharedDocuments, ZonedDateTime sharedAtTime, ZonedDateTime expiryTime, ZonedDateTime withdrawnTime) {
        this.sharedDocuments = sharedDocuments;
        this.sharedAtTime = sharedAtTime;
        this.expiryTime = expiryTime;
        this.withdrawnTime = withdrawnTime;
    }

    public List<SharedDocument> getSharedDocuments() {
        return sharedDocuments;
    }

    public void setSharedDocuments(List<SharedDocument> sharedDocuments) {
        this.sharedDocuments = sharedDocuments;
    }

    public ZonedDateTime getSharedAtTime() {
        return sharedAtTime;
    }

    public void setSharedAtTime(ZonedDateTime sharedAtTime) {
        this.sharedAtTime = sharedAtTime;
    }

    public ZonedDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(ZonedDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public ZonedDateTime getWithdrawnTime() {
        return withdrawnTime;
    }

    public void setWithdrawnTime(ZonedDateTime withdrawnTime) {
        this.withdrawnTime = withdrawnTime;
    }

    public void addSharedDocument(SharedDocument sharedDocument) {
        this.sharedDocuments.add(sharedDocument);
    }

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        return this.links;
    }

    protected void setLink(final List<Link> links) {
        this.links = links;
    }

    public URI stopSharing() {
        Link link = getLinkByRelationName(STOP_SHARING);
        return link != null ? link.getUri() : null;
    }

    @Override
    public String toString() {
        return "ShareDocumentsRequestState{" +
                "sharedDocuments=" + sharedDocuments +
                ", sharedAtTime=" + sharedAtTime +
                ", expiryTime=" + expiryTime +
                ", withdrawnTime=" + withdrawnTime +
                ", links=" + links +
                '}';
    }
}
