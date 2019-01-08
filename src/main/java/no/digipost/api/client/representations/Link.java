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

import static no.digipost.api.client.representations.Relation.UNSUPPORTED;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "link", propOrder = { "rel", "uri", "mediaType" })
public class Link {
    private static final String RELATIONS_BASE_PATH = "/relations";

    @XmlTransient
    private Relation rel;
    @XmlTransient
    private String relationBaseUri;

    @XmlAttribute(required = true)
    private String uri;
    @XmlAttribute(name = "media-type")
    private String mediaType;

    public Link(final Relation relation, final DigipostUri uri, final String mediaType) {
        rel = relation;
        relationBaseUri = uri.getBaseUri() + RELATIONS_BASE_PATH;
        this.uri = uri.getUri().toString();
        this.mediaType = mediaType;
    }

    public Link(final Relation relation, final DigipostUri uri) {
        this(relation, uri, MediaTypes.DIGIPOST_MEDIA_TYPE_V7);
    }

    Link() {
    }

    private String parseRelationsBaseUri(final String rel) {
        return rel.substring(0, rel.lastIndexOf("/"));
    }

    private Relation parseRel(final String rel) {
        if (rel == null || rel.isEmpty()) return UNSUPPORTED;
        try {
            return Relation.valueOf(rel.substring(rel.lastIndexOf("/") + 1).toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNSUPPORTED;
        }
    }

    public String getRelationUri() {
        return relationBaseUri + "/" + rel.name().toLowerCase();
    }

    @XmlAttribute
    public String getRel() {
        return getRelationUri();
    }

    public void setRel(final String rel) {
        this.rel = parseRel(rel);
        relationBaseUri = parseRelationsBaseUri(rel);
    }

    public Relation getRelationName() {
        return rel;
    }

    public URI getUri() {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equalsRelation(final Relation relation) {
        return rel.equals(relation);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(31, 1).append(mediaType).append(rel).append(relationBaseUri).append(uri).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Link other = (Link) obj;
        return new EqualsBuilder()
                .append(mediaType, other.mediaType)
                .append(rel, other.rel)
                .append(relationBaseUri, other.relationBaseUri)
                .append(uri, other.uri)
                .isEquals();
    }
}
