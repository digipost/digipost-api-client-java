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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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

    public URI getInboxUri(){
        return getLinkByRelationName(GET_INBOX).getUri();
    }
    
    public URI getCreateMessageUri() {
        return getLinkByRelationName(CREATE_MESSAGE).getUri();
    }

    public URI getArchiveDocumentsUri() {
        return getLinkByRelationName(ARCHIVE_DOCUMENTS).getUri();
    }
    
    public URI getArchivesUri() {
        return getLinkByRelationName(GET_ARCHIVES).getUri();
    }
    
    public URI getCreateBatch() {
        return getLinkByRelationName(CREATE_BATCH).getUri();
    }
    
    public URI getBatchByUUID(UUID uuid) {
        return getLinkByRelationName(GET_BATCH).getUri().resolve(uuid.toString());
    }
    
    public URI getArchiveDocumentByUUIDUri(UUID uuid) {
        return getLinkByRelationName(GET_ARCHIVE_DOCUMENT_BY_UUID).getUri().resolve(uuid.toString());
    }

    public URI getArchiveDocumentByReferenceUri(String referenceid) {
        final String referenceIdBase64 = Base64.getEncoder().encodeToString(referenceid.getBytes(StandardCharsets.UTF_8));
        return getLinkByRelationName(GET_ARCHIVE_DOCUMENTS_BY_REFERENCEID).getUri().resolve(referenceIdBase64);
    }

    public URI getPrintEncryptionCertificate() {
        return getLinkByRelationName(GET_PRINT_ENCRYPTION_CERTIFICATE).getUri();
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

    public URI getAddTagUri() { return getLinkByRelationName(ADD_TAG).getUri(); }

    public URI getRemoveTagUri() { return getLinkByRelationName(REMOVE_TAG).getUri(); }

    public URI getTagsUri() { return getLinkByRelationName(GET_TAGS).getUri(); }

    public URI getShareDocumentsRequestStateUri() { return getLinkByRelationName(GET_SHARE_DOCUMENTS_REQUEST_STATE).getUri(); }

    public URI getCreateOrActivateUserAccountUri() { return getLinkByRelationName(CREATE_OR_ACTIVATE_USER_ACCOUNT).getUri(); }

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
