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

import no.digipost.api.client.representations.ContentHash;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;
import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.time.Clock;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static no.digipost.api.client.representations.Relation.ADD_UNIQUE_UUID;
import static no.digipost.api.client.representations.Relation.GET_ARCHIVE_DOCUMENT_BY_UUID;
import static no.digipost.api.client.representations.Relation.GET_ARCHIVE_DOCUMENT_CONTENT;
import static no.digipost.api.client.representations.Relation.GET_ARCHIVE_DOCUMENT_CONTENT_STREAM;
import static no.digipost.api.client.representations.Relation.SELF_DELETE;
import static no.digipost.api.client.representations.Relation.SELF_UPDATE;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive-document", propOrder = {
        "uuid",
        "fileName",
        "fileType",
        "referenceid",
        "contentType",
        "contentHash",
        "attributes",
        "archivedTime",
        "deletionTime",
        "links"
})
@XmlRootElement(name = "archive-document")
public class ArchiveDocument extends Representation {

    @XmlElement(required = true)
    public final UUID uuid;
    @XmlElement(name = "file-name", nillable = false)
    protected String fileName;
    @XmlElement(name = "file-type", required = true)
    protected String fileType;
    @XmlElement(nillable = false)
    protected String referenceid;
    @XmlElement(name = "content-type", nillable = false)
    protected String contentType;
    @XmlElement(name = "content-hash", nillable = false)
    protected ContentHash contentHash;

    @XmlElement(nillable = false)
    protected List<ArchiveDocumentAttribute> attributes;

    @XmlElement(name = "archived-time", type = String.class, nillable = false)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected ZonedDateTime archivedTime;
    @XmlElement(name = "deletion-time", type = String.class, nillable = false)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected ZonedDateTime deletionTime;
    public ArchiveDocument() {
        this(null, null, null, null);
    }

    public ArchiveDocument(final UUID uuid, final String fileName, final String fileType, final String contentType) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileType = fileType;
        this.contentType = contentType;
        this.attributes = new ArrayList<>();
    }

    public ArchiveDocument withAttribute(String key, String value) {
        // check duplicate and overwrite value
        final Optional<ArchiveDocumentAttribute> first = this.attributes.stream().filter(s -> s.key.equals(key)).findFirst();
        if (first.isPresent()) {
            first.get().value = value;
        } else {
            this.attributes.add(new ArchiveDocumentAttribute(key, value));
        }
        return this;
    }

    public ArchiveDocument withAttributes(Map<String, String> attributes) {
        attributes.forEach(this::withAttribute);
        return this;
    }

    public ArchiveDocument withReferenceId(final String referenceid) {
        this.referenceid = referenceid;
        return this;
    }

    public ArchiveDocument withDeletionTime(final ZonedDateTime deletionTime) {
        this.deletionTime = deletionTime;
        return this;
    }

    public ArchiveDocument withDeleteAfter(Period duration, Clock clock) {
        this.deletionTime = ZonedDateTime.now(clock).plus(duration);
        return this;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getReferenceid() {
        return referenceid;
    }

    public String getContentType() {
        return contentType;
    }

    public ContentHash getContentHash() {
        return contentHash;
    }

    public void setContentHash(ContentHash value) {
        this.contentHash = value;
    }

    public List<ArchiveDocumentAttribute> getAttributes() {
        return attributes;
    }

    public ZonedDateTime getArchivedTime() {
        return archivedTime;
    }

    public ZonedDateTime getDeletionTime() {
        return deletionTime;
    }

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        return links;
    }

    public URI getDocumentByUUID() {
        return getLinkByRelationName(GET_ARCHIVE_DOCUMENT_BY_UUID).getUri();
    }

    public URI getDocumentContent() {
        return getLinkByRelationName(GET_ARCHIVE_DOCUMENT_CONTENT).getUri();
    }

    public URI getUpdate() {
        return getLinkByRelationName(SELF_UPDATE).getUri();
    }

    public URI getDelete() {
        return getLinkByRelationName(SELF_DELETE).getUri();
    }

    public URI getAddUniqueUUID() {
        return getLinkByRelationName(ADD_UNIQUE_UUID).getUri();
    }

    public URI getDocumentContentStream() {
        return getLinkByRelationName(GET_ARCHIVE_DOCUMENT_CONTENT_STREAM).getUri();
    }

    @Override
    public String toString() {
        return "ArchiveDocument{" +
                "uuid=" + uuid +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", referenceid='" + referenceid + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentHash=" + contentHash.getHashAlgorithm() + ":" + contentHash.getHash() +
                ", attributes=" + attributes +
                ", archivedTime=" + archivedTime +
                ", deletionTime=" + deletionTime +
                '}';
    }
}
