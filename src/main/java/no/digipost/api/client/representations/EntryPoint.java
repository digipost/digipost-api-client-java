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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.net.URI;
import java.util.List;

import static no.digipost.api.client.representations.Relation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "entrypoint")
public class EntryPoint extends Representation {

    @XmlElement(name = "certificate", required = false)
    private String certificate;

    public EntryPoint(final String certificate, final Link... links) {
        super(links);
        this.certificate = certificate;
    }

    public EntryPoint() {
    }

    public URI getCreateMessageUri() {
        return getLinkByRelationName(CREATE_MESSAGE).getUri();
    }

    public URI getPrintEncryptionKey() {
        return getLinkByRelationName(GET_PRINT_ENCRYPTION_KEY).getUri();
    }

    public URI getSearchUri() {
        return getLinkByRelationName(SEARCH).getUri();
    }

    public URI getAutocompleteUri() {
        return getLinkByRelationName(AUTOCOMPLETE).getUri();
    }

    public URI getIdentificationUri() {
        return getLinkByRelationName(IDENTIFY_RECIPIENT).getUri();
    }

    public URI getIdentificationWithEncryptionKeyUri() {
        return getLinkByRelationName(IDENTIFY_RECIPIENT_WITH_ENCRYPTION_KEY).getUri();
    }

    public URI getDocumentEventsUri() {
        return getLinkByRelationName(DOCUMENT_EVENTS).getUri();
    }

    public URI getSenderInformationUri() {
        return getLinkByRelationName(GET_SENDER_INFORMATION).getUri();
    }

    public String getCertificate() {
        return certificate;
    }

    @XmlElement(name = "link")
    protected List<Link> getLink() {
        return links;
    }

    protected void setLink(final List<Link> links) {
        this.links = links;
    }
}
