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
package no.digipost.api.client.representations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;
import java.util.UUID;

import static no.digipost.api.client.representations.Relation.GET_DOCUMENT_CONTENT;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document-metadata")
public class DocumentMetadata extends Representation {

    @XmlAttribute(name = "uuid", required = true)
    public final UUID uuid;
    @XmlAttribute(name = "technical-type")
    public final String technicalType;

    @XmlElement(name = "link")
    protected List<Link> getLinks() {
        return links;
    }

    public DocumentMetadata() {
        this(null, null);
    }

    public DocumentMetadata(UUID uuid, String technicalType, Link... links) {
        super(links);
        this.uuid = uuid;
        this.technicalType = technicalType;
    }

    public Link getDocumentContentLink() {
        return getLinkByRelationName(GET_DOCUMENT_CONTENT);
    }
}
