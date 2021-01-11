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

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;
import no.digipost.api.client.representations.SenderOrganization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive", propOrder = {
    "senderOrganization",
    "senderId",
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
    protected String name;
    @XmlElement(nillable = false)
    protected List<ArchiveDocument> documents;

    public static ArchiveBuilder defaultArchive(){
        return new ArchiveBuilder();
    }

    public Archive() {
        super();
        this.documents = new ArrayList<>();
    }

    public Archive(final SenderOrganization senderOrganization, final Long senderId, final String name, final List<Link> links, final List<ArchiveDocument> documents) {
        this.senderOrganization = senderOrganization;
        this.senderId = senderId;
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

    public String getName() {
        return name;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    public List<ArchiveDocument> getDocuments() {
        return this.documents;
    }

    public static class ArchiveBuilder {

        private Long senderId;
        private SenderOrganization senderOrganization;
        private final List<ArchiveDocument> documents = new ArrayList<>();

        private ArchiveBuilder() {
        }

        /**
         * Only neccessary when sending on behalf of another user. In this case
         * senderId must be the party you are sending on behalf of. Your own user id
         * should be set in the http header X-Digipost-UserId.
         */
        public ArchiveBuilder senderId(SenderId senderId) {
            this.senderId = senderId.value();
            return this;
        }

        /**
         * Only neccessary when sending on behalf of another user. In this case
         * senderOrganization must be the party you are sending on behalf of.
         * Your own user id should be set in the http header X-Digipost-UserId.
         */
        public ArchiveBuilder senderOrganization(SenderOrganization senderOrganization) {
            this.senderOrganization = senderOrganization;
            return this;
        }

        public ArchiveBuilder documents(ArchiveDocument... documents) {
            return documents(asList(documents));
        }

        public ArchiveBuilder documents(Iterable<ArchiveDocument> documents) {
            defaultIfNull(documents, Collections.<ArchiveDocument>emptyList()).forEach(this.documents::add);
            return this;
        }

        public Archive build() {
            if (senderId != null && senderOrganization != null) {
                throw new IllegalStateException("You can't set both senderId *and* senderOrganization.");
            }

            return new Archive(this.senderOrganization, this.senderId, null, null, this.documents);
        }
    }

}
