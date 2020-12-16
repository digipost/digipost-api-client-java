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
package no.digipost.api.client.representations.archive;

import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;
import no.digipost.api.client.representations.SenderOrganization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive", propOrder = {
    "senderOrganization",
    "senderId",
    "uuid",
    "name",
    "documents"
})
@XmlRootElement(name = "archive")
public class Archive extends Representation {

    @XmlElement(name = "sender-organization", nillable = false)
    protected SenderOrganization senderOrganization;
    @XmlElement(name = "sender-id", nillable = false)
    protected Long senderId;
    @XmlElement(nillable = false)
    protected String uuid;
    @XmlElement(nillable = false)
    protected String name;
    @XmlElement(nillable = false)
    protected List<ArchiveDocument> documents;

    public Archive() {
        super();
        this.documents = new ArrayList<>();
    }

    public Archive(final SenderOrganization senderOrganization, final Long senderId, final String uuid, final String name, final List<Link> links, final List<ArchiveDocument> documents) {
        this.senderOrganization = senderOrganization;
        this.senderId = senderId;
        this.uuid = uuid;
        this.name = name;
        this.links = links;
        this.documents = documents;
    }

    public SenderOrganization getSenderOrganization() {
        return senderOrganization;
    }

    public void setSenderOrganization(SenderOrganization value) {
        this.senderOrganization = value;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    public List<ArchiveDocument> getDocuments() {
        return this.documents;
    }

}
