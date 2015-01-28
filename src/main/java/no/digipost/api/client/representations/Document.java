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

import no.motif.f.Predicate;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

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
		"preEncrypt",
		"contentHash",
		"links"
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
	@XmlElement(name = "pre-encrypt")
	protected Boolean preEncrypt;
	@XmlElement(name = "content-hash", nillable = false)
	protected ContentHash contentHash;

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
	public Document(String id, String subject, FileType fileType) {
		this(id, subject, fileType, null, null, null, null, null, null, null);
	}

	public Document(String uuid, String subject, FileType fileType, String openingReceipt,
					SmsNotification smsNotification, EmailNotification emailNotification,
					AuthenticationLevel authenticationLevel,
					SensitivityLevel sensitivityLevel) {
		this(uuid, subject, fileType, openingReceipt, smsNotification, emailNotification, authenticationLevel, sensitivityLevel, null, null);
	}


	public Document(String uuid, String subject, FileType fileType, String openingReceipt,
					SmsNotification smsNotification, EmailNotification emailNotification,
					AuthenticationLevel authenticationLevel,
					SensitivityLevel sensitivityLevel, Boolean opened, String technicalType) {
		this.uuid = lowerCase(uuid);
		this.subject = subject;
		this.digipostFileType = Objects.toString(fileType, null);
		this.openingReceipt = defaultIfBlank(openingReceipt, null);
		this.opened = opened == Boolean.TRUE ? true : null;
		this.smsNotification = smsNotification;
		this.emailNotification = emailNotification;
		this.authenticationLevel = authenticationLevel;
		this.sensitivityLevel = sensitivityLevel;
		this.technicalType = technicalType;
		validate();
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

	public static Document technicalAttachment(String type, FileType fileType) {
		Document document = new Document(UUID.randomUUID().toString(), null, fileType);
		document.technicalType = type;
		return document;
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

	public Document setPreEncrypt() {
		this.preEncrypt = true;
		return this;
	}

	public static final Predicate<Document> isPreEncrypt = new Predicate<Document>() { @Override public boolean $(Document document) {
		return document.isPreEncrypt();
    }};

	public boolean isPreEncrypt() {
		return preEncrypt != null && preEncrypt;
	}

	public Link getAddContentLink() {
		return getLinkByRelationName(Relation.ADD_CONTENT);
	}

	public Link getEncryptionKeyLink() { return getLinkByRelationName(Relation.GET_ENCRYPTION_KEY); }

	public String getUuid() {
		return uuid;
	}

	public String getTechnicalType() {
		return technicalType;
	}

	public boolean isOpened() {
		return opened != null && opened;
	}
}
