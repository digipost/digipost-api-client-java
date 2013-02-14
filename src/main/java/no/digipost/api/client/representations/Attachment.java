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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attachment", propOrder = { "subject", "links" })
@XmlRootElement(name = "attachment")
public class Attachment extends Representation {

	@XmlElement(name = "subject", required = true)
	protected String subject;

	public Attachment() {
	}

	public Attachment(final String subject) {
		this.subject = subject;
	}

	@XmlElement(name = "link")
	protected List<Link> getLinks() {
		return links;
	}

	protected void setLinks(final List<Link> links) {
		this.links = links;
	}

	public String getSubject() {
		return subject;
	}

	public Link getSelfLink() {
		return getLinkByRelationName(Relation.SELF);
	}

	public Link getAddContentLink() {
		return getLinkByRelationName(Relation.ADD_CONTENT);
	}

	public boolean isSameAttachmentAs(final Attachment attachment) {
		return subject.equals(attachment.getSubject());
	}

}
