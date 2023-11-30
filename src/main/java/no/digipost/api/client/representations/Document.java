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
package no.digipost.api.client.representations;

import no.digipost.api.datatypes.DataType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.join;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document", propOrder = {
        "uuid",
        "subject",
        "digipostFileType",
        "opened",
        "openingReceipt",
        "smsNotification",
        "emailNotification",
        "authenticationLevel",
        "sensitivityLevel",
        "encrypted",
        "contentHash",
        "links",
        "dataType"
})
public class Document extends Representation {

    @XmlElement(name = "uuid", required = true)
    public final UUID uuid;
    @XmlElement(name = "subject", required = true)
    public final String subject;
    @XmlElement(name = "file-type", required = true)
    protected String digipostFileType;
    @XmlElement(nillable = false)
    protected Boolean opened;
    @XmlElement(name = "opening-receipt")
    public final String openingReceipt;
    @XmlElement(name = "sms-notification")
    public final SmsNotification smsNotification;
    @XmlElement(name = "email-notification")
    public final EmailNotification emailNotification;
    @XmlElement(name = "authentication-level")
    public final AuthenticationLevel authenticationLevel;
    @XmlElement(name = "sensitivity-level")
    public final SensitivityLevel sensitivityLevel;
    @XmlElement(name = "encrypted")
    protected EncryptionInfo encrypted;
    @XmlElement(name = "content-hash", nillable = false)
    protected ContentHash contentHash;

    @XmlElement(name="data-type")
    protected DataTypeHolder dataType;

    @XmlElement(name = "link")
    protected List<Link> getLinks() {
        return links;
    }

    @XmlAttribute(name = "technical-type")
    private String technicalType;

    public Document() { this(null, null, null); }

    /**
     * Constructor for just the required fields of a document.
     */
    public Document(UUID uuid, String subject, FileType fileType) {
        this(uuid, subject, fileType, null, null, null, null, null, null, null, (String[]) null);
    }

    public Document(UUID uuid, String subject, FileType fileType, DataType data) {
        this(uuid, subject, fileType, null, null, null, null, null, null, data, (String[]) null);
    }

    public Document(UUID uuid, String subject, FileType fileType, String openingReceipt,
                    SmsNotification smsNotification, EmailNotification emailNotification,
                    AuthenticationLevel authenticationLevel,
                    SensitivityLevel sensitivityLevel) {
        this(uuid, subject, fileType, openingReceipt, smsNotification, emailNotification, authenticationLevel, sensitivityLevel, null, null, (String[]) null);
    }


    public Document(UUID uuid, String subject, FileType fileType, String openingReceipt,
                    SmsNotification smsNotification, EmailNotification emailNotification,
                    AuthenticationLevel authenticationLevel,
                    SensitivityLevel sensitivityLevel, Boolean opened, DataType data, String... technicalType) {
        this.uuid = uuid;
        this.subject = subject;
        this.digipostFileType = Objects.toString(fileType, null);
        this.openingReceipt = defaultIfBlank(openingReceipt, null);
        this.opened = Boolean.TRUE.equals(opened) ? true : null;
        this.smsNotification = smsNotification;
        this.emailNotification = emailNotification;
        this.authenticationLevel = authenticationLevel;
        this.sensitivityLevel = sensitivityLevel;
        this.technicalType = parseTechnicalTypes(technicalType);
        this.dataType = data != null ? new DataTypeHolder(data) : null;
        this.validate();
    }

    static String parseTechnicalTypes(String... technicalTypes){
        if(technicalTypes == null || technicalTypes.length == 0) {
            return null;
        }

        return Stream.of(technicalTypes)
            .filter(s -> Objects.nonNull(s) && !s.isEmpty())
            .map(String::trim)
            .collect(collectingAndThen(joining(","), joined -> joined.isEmpty() ? null : joined));
    }

    public Document copyDocumentAndSetDigipostFileTypeToPdf(){
        Document newDoc = new Document(this.uuid, this.subject, new FileType("pdf"), this.openingReceipt, this.smsNotification, this.emailNotification,
                this.authenticationLevel, this.sensitivityLevel, this.opened, this.dataType != null ? this.dataType.get() : null, this.getTechnicalType());

        newDoc.encrypted  = this.encrypted == null ? null : this.encrypted.copy();
        newDoc.setContentHash(this.contentHash);

        return newDoc;
    }

    private void validate() {
        List<String> errors = new ArrayList<>();
        if (openingReceipt != null && opened != null) {
            errors.add("Both openingReceipt and opened was set");
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                    errors.size() + " errors when instantiating " + Document.class.getSimpleName() +
                    "\n - " + join(errors, "\n - "));
        }
    }

    public static Document technicalAttachment(FileType fileType, String... type) {
        Document document = new Document(UUID.randomUUID(), null, fileType);
        document.technicalType = parseTechnicalTypes(type);
        return document;
    }

    public void setContentHash(ContentHash contentHash){
        this.contentHash = contentHash;
    }

    public void setDigipostFileType(FileType fileType) {
        this.digipostFileType = fileType.toString();
    }

    public String getDigipostFileType() {
        return digipostFileType;
    }

    public boolean is(FileType fileType) {
        return fileType.equals(new FileType(digipostFileType));
    }

    public Document encrypt() {
        if (this.encrypted != null) {
            throw new IllegalStateException("Document already set to encrypted, are you calling encrypt() twice?");
        }
        this.encrypted = new EncryptionInfo();
        return this;
    }

    public boolean willBeEncrypted() {
        return encrypted != null;
    }

    public EncryptionInfo getEncrypted() {
        return encrypted;
    }

    public Link getAddContentLink() {
        return getLinkByRelationName(Relation.ADD_CONTENT);
    }

    public AddDataLink getAddDataLink() {
        return new AddDataLink(getLinkByRelationName(Relation.ADD_DATA).getUri().getPath());
    }

    public Link getEncryptionKeyLink() {
        return getLinkByRelationName(Relation.GET_ENCRYPTION_KEY);
    }

    public String[] getTechnicalType() {
        return technicalType != null ? technicalType.split(",") : null;
    }

    public boolean isOpened() {
        return opened != null && opened;
    }

    public Optional<DataType> getDataType() {
        return Optional.ofNullable(dataType).map(DataTypeHolder::get);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " with uuid '" + uuid + "'" +
                ofNullable(technicalType).map(t -> ", technicalType '" + t + "'").orElse("") +
                (willBeEncrypted() ? ofNullable(subject).map(s -> ", subject '" + s + "'").orElse(", no subject") : ", encrypted");
    }

    public void setNumberOfEncryptedPages(int pages) {
        if (this.encrypted == null) {
            throw new IllegalStateException("Tried setting number of encrypted pages, but document is not set to be encrypted. Have you called Document.encrypt()?");
        }
        this.encrypted.setNumberOfPages(pages);
    }
}
