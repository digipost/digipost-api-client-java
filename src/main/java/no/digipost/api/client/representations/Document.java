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

import javax.xml.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.lowerCase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document", propOrder = {
		"id",
		"subject",
		"digipostFileType",
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

	@XmlElement(name = "id", required = true)
	public final String id;
	@XmlElement(name = "subject", required = true)
	public final String subject;
	@XmlElement(name = "file-type", required = true)
	protected String digipostFileType;
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

	public Document() { this(null, null, null); }

	/**
	 * Constructor for just the required fields of a document.
	 */
	public Document(String id, String subject, FileType fileType) {
		this(id, subject, fileType, null, null, null, null, null);
	}

	public Document(String id, String subject, FileType fileType, String openingReceipt,
					SmsNotification smsNotification, EmailNotification emailNotification,
					AuthenticationLevel authenticationLevel,
					SensitivityLevel sensitivityLevel) {
		this.id = lowerCase(id);
		this.subject = subject;
		this.digipostFileType = Objects.toString(fileType, null);
		this.openingReceipt = openingReceipt;
		this.smsNotification = smsNotification;
		this.emailNotification = emailNotification;
		this.authenticationLevel = authenticationLevel;
		this.sensitivityLevel = sensitivityLevel;
	}

	public void setDigipostFileType(FileType fileType) {
		this.digipostFileType = fileType.toString();
	}

	public String getDigipostFileType() {
		return digipostFileType;
	}

	public void setPreEncrypt() {
		this.preEncrypt = true;
	}

	public boolean isPreEncrypt() {
		return preEncrypt != null && preEncrypt;
	}

	public Link getAddContentLink() {
		return getLinkByRelationName(Relation.ADD_CONTENT);
	}

	public Link getEncryptionKeyLink() { return getLinkByRelationName(Relation.GET_ENCRYPTION_KEY); }

	public String getUuid() {
		return id;
	}
}
