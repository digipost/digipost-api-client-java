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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static no.digipost.api.client.representations.Relation.GET_ARCHIVE_DOCUMENT_BY_UUID;
import static no.digipost.api.client.representations.Relation.GET_ARCHIVE_DOCUMENT_CONTENT;
import static no.digipost.api.client.representations.Relation.GET_ARCHIVE_DOCUMENT_CONTENT_STREAM;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive-document", propOrder = {
        "uuid",
        "fileName",
        "fileType",
        "referenceid",
        "contentType",
        "contentHash",
        "archivedTime",
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
    @XmlElement(name = "archived-time", type = String.class, nillable = false)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected ZonedDateTime archivedTime;

    public ArchiveDocument() {
        this(null, null, null, null, null);
    }

    public ArchiveDocument(final UUID uuid, final String fileName, final String fileType, final String contentType) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileType = fileType;
        this.referenceid = referenceid;
        this.contentType = contentType;
    }

    public ArchiveDocument(final UUID uuid, final String fileName, final String fileType, final String contentType, final String referenceid) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileType = fileType;
        this.referenceid = referenceid;
        this.contentType = contentType;
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

    public ZonedDateTime getArchivedTime() {
        return archivedTime;
    }

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        return links;
    }

    public Optional<URI> getDocumentByUUID() {
        return Optional.ofNullable(getLinkByRelationName(GET_ARCHIVE_DOCUMENT_BY_UUID)).map(Link::getUri);
    }

    public Optional<URI> getDocumentContent() {
        return Optional.ofNullable(getLinkByRelationName(GET_ARCHIVE_DOCUMENT_CONTENT)).map(Link::getUri);
    }

    public Optional<URI> getDocumentContentStream() {
        return Optional.ofNullable(getLinkByRelationName(GET_ARCHIVE_DOCUMENT_CONTENT_STREAM)).map(Link::getUri);
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
                ", archivedTime=" + archivedTime +
                '}';
    }
}
