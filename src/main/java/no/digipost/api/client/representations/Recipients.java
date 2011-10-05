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

import static no.digipost.api.client.representations.Relation.SELF;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Recipients extends Representation {

	@XmlElement(name = "recipient")
	private final List<Recipient> recipients;

	public Recipients(final Link... links) {
		super(links);
		recipients = new LinkedList<Recipient>();
	}

	public Recipients() {
		recipients = new LinkedList<Recipient>();
	}

	public List<Recipient> getRecipients() {
		return recipients;
	}

	public Link getSelfUri() {
		return getLinkByRelationName(SELF);
	}

	public void add(final Recipient recipient) {
		recipients.add(recipient);
	}

	@XmlElement(name = "link")
	protected List<Link> getLink() {
		return links;
	}

	protected void setLink(final List<Link> links) {
		this.links = links;
	}
}
