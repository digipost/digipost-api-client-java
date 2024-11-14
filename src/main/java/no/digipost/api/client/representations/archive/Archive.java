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
package no.digipost.api.client.representations.archive;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;
import no.digipost.api.client.representations.SenderOrganization;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.Relation.NEXT_DOCUMENTS;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive", propOrder = {
        "senderOrganization",
        "senderId",
        "name",
        "documents",
        "links"
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

    public static ArchiveBuilder defaultArchive() {
        return new ArchiveBuilder();
    }

    public static ArchiveBuilder namedArchive(String archiveName) {
        return new ArchiveBuilder(archiveName);
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

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        return this.links;
    }

    protected void setLink(final List<Link> links) {
        this.links = links;
    }

    public List<ArchiveDocument> getDocuments() {
        return this.documents;
    }

    public Optional<URI> getNextDocuments() {
        return Optional.ofNullable(getLinkByRelationName(NEXT_DOCUMENTS)).map(Link::getUri);
    }

    public Optional<URI> getNextDocumentsWithAttributes(Map<String, String> attributes) {
        final String attributesCommaSeparated = attributes.entrySet().stream().flatMap(en -> Stream.of(en.getKey(), en.getValue())).collect(Collectors.joining(","));

        return Optional.ofNullable(getLinkByRelationName(NEXT_DOCUMENTS)).map(Link::getUri)
                .map(uri -> {
                    try {
                        return new URIBuilder(uri)
                                .addParameter("attributes", Base64.getEncoder().encodeToString(attributesCommaSeparated.getBytes(StandardCharsets.UTF_8)))
                                .build();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Optional<URI> getNextDocumentsWithAttributesByDate(Map<String, String> attributes, OffsetDateTime from, OffsetDateTime to) {
        final String attributesCommaSeparated = attributes.entrySet().stream().flatMap(en -> Stream.of(en.getKey(), en.getValue())).collect(Collectors.joining(","));
        
        return Optional.ofNullable(getLinkByRelationName(NEXT_DOCUMENTS)).map(Link::getUri)
                .map(uri -> {
                    try {
                        return new URIBuilder(uri)
                                .addParameter("attributes", base64(attributesCommaSeparated))
                                .addParameter("fromDate", base64(from.toString()))
                                .addParameter("toDate", base64(to.toString()))
                                .build();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Optional<URI> getNextDocumentsByDate(OffsetDateTime from, OffsetDateTime to) {
        
        return Optional.ofNullable(getLinkByRelationName(NEXT_DOCUMENTS)).map(Link::getUri)
                .map(uri -> {
                    try {
                        return new URIBuilder(uri)
                                .addParameter("fromDate", base64(from.toString()))
                                .addParameter("toDate", base64(to.toString()))
                                .build();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public static class ArchiveBuilder {
        private String name;
        private Long senderId;
        private SenderOrganization senderOrganization;
        private final List<ArchiveDocument> documents = new ArrayList<>();

        private ArchiveBuilder() {
        }

        private ArchiveBuilder(String name) {
            this.name = name;
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

            return new Archive(this.senderOrganization, this.senderId, this.name, null, this.documents);
        }
    }

    private static String base64(String param){
        return Base64.getEncoder().encodeToString(param.getBytes(StandardCharsets.UTF_8));
    }
    
}
