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

import no.digipost.api.datatypes.DataType;
import no.digipost.api.datatypes.types.Appointment;
import no.digipost.api.datatypes.types.Residence;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.lowerCase;

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
        "metadata"
})
@XmlSeeAlso({ Invoice.class })
public class Document extends Representation {

    private final static Pattern UUID_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");


    @XmlElement(name = "uuid", required = true)
    public final String uuid;
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

    @XmlElementWrapper(name="metadata")
    @XmlElements({
            @XmlElement(type = Residence.class, namespace = "http://api.digipost.no/schema/datatypes"),
            @XmlElement(name = "appointment", type = Appointment.class, namespace = "http://api.digipost.no/schema/datatypes")
    })
    protected List<DataType> metadata;

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
    public Document(String uuid, String subject, FileType fileType) {
        this(uuid, subject, fileType, null, null, null, null, null, null, null, (String[]) null);
    }

    public Document(String uuid, String subject, FileType fileType, String openingReceipt,
                    SmsNotification smsNotification, EmailNotification emailNotification,
                    AuthenticationLevel authenticationLevel,
                    SensitivityLevel sensitivityLevel) {
        this(uuid, subject, fileType, openingReceipt, smsNotification, emailNotification, authenticationLevel, sensitivityLevel, null, null, (String[]) null);
    }


    public Document(String uuid, String subject, FileType fileType, String openingReceipt,
                    SmsNotification smsNotification, EmailNotification emailNotification,
                    AuthenticationLevel authenticationLevel,
                    SensitivityLevel sensitivityLevel, Boolean opened, List<DataType> metadata, String... technicalType) {
        this.uuid = lowerCase(uuid);
        this.subject = subject;
        this.digipostFileType = Objects.toString(fileType, null);
        this.openingReceipt = defaultIfBlank(openingReceipt, null);
        this.opened = opened == Boolean.TRUE ? true : null;
        this.smsNotification = smsNotification;
        this.emailNotification = emailNotification;
        this.authenticationLevel = authenticationLevel;
        this.sensitivityLevel = sensitivityLevel;
        this.technicalType = parseTechnicalTypes(technicalType);
        this.metadata = metadata;
        validate();
    }

    static String parseTechnicalTypes(String... technicalTypes){
        if(technicalTypes == null || technicalTypes.length == 0){
            return null;
        }

        Set<String> cleanedStrings = new HashSet<>();
        for(String st : technicalTypes){
            if(st != null && !st.isEmpty()){
                cleanedStrings.add(st.trim());
            }
        }

        return cleanedStrings.size() != 0 ? StringUtils.join(cleanedStrings, ",") : null;

    }

    public Document copyDocumentAndSetDigipostFileTypeToPdf(){
        Document newDoc = new Document(this.uuid, this.subject, new FileType("pdf"), this.openingReceipt, this.smsNotification, this.emailNotification,
                this.authenticationLevel, this.sensitivityLevel, this.opened, this.metadata, this.getTechnicalType());

        newDoc.encrypted  = this.encrypted == null ? null : this.encrypted.copy();
        newDoc.setContentHash(this.contentHash);

        return newDoc;
    }

    private void validate() {
        List<String> errors = new ArrayList<>();
        if (uuid != null && !UUID_PATTERN.matcher(this.uuid).matches()) {
            errors.add("Not a UUID: " + uuid);
        }
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
        Document document = new Document(UUID.randomUUID().toString(), null, fileType);
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
        return  encrypted != null;
    }

    public EncryptionInfo getEncrypted() {
        return encrypted;
    }

    public Link getAddContentLink() {
        return getLinkByRelationName(Relation.ADD_CONTENT);
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
