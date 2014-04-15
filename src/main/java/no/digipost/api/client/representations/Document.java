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
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "document", propOrder = {
		"uuid",
		"subject",
		"digipostFileType",
		"openingReceipt",
		"smsNotification",
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
	@XmlElement(name = "opening-receipt")
	public final String openingReceipt;
	@XmlElement(name = "sms-notification")
	public final SmsNotification smsNotification;
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

	Document() { this(null, null, null); }

	/**
	 * Constructor for just the required fields of a document.
	 */
	public Document(String uuid, String subject, FileType fileType) {
		this(uuid, subject, fileType, null, null, null, null);
	}

	public Document(String uuid, String subject, FileType fileType, String openingReceipt,
					SmsNotification smsNotification, AuthenticationLevel authenticationLevel,
					SensitivityLevel sensitivityLevel) {
		this.uuid = org.apache.commons.lang3.StringUtils.lowerCase(uuid);
		if (uuid != null && !UUID_PATTERN.matcher(this.uuid).matches()) {
			throw new IllegalArgumentException("Not a UUID: " + uuid);
		}
		this.subject = subject;
		this.digipostFileType = Objects.toString(fileType, null);
		this.openingReceipt = openingReceipt;
		this.smsNotification = smsNotification;
		this.authenticationLevel = authenticationLevel;
		this.sensitivityLevel = sensitivityLevel;
	}

	public void setDigipostFileType(FileType fileType) {
		this.digipostFileType = fileType.toString();
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

	public String getUuid() {
		return uuid;
	}
}
