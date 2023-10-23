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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "shared-documents-request-state", propOrder = {
        "sharedDocuments",
        "sharedAtTime",
        "expiryTime",
        "withdrawnTime",
        "links"
})
@XmlRootElement(name = "shared-documents-request-state")
public class SharedDocumentsRequestState extends Representation {

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

    public SharedDocumentsRequestState() {
    }

    public SharedDocumentsRequestState(List<SharedDocument> sharedDocuments, ZonedDateTime sharedAtTime, ZonedDateTime expiryTime, ZonedDateTime withdrawnTime) {
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
}
