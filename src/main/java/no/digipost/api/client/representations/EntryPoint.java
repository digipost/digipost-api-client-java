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

import static no.digipost.api.client.representations.Relation.AUTOCOMPLETE;
import static no.digipost.api.client.representations.Relation.CREATE_MESSAGE;
import static no.digipost.api.client.representations.Relation.CREATE_PRINT_MESSAGE;
import static no.digipost.api.client.representations.Relation.SEARCH;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "links")
public class EntryPoint extends Representation {

	public EntryPoint(final Link... links) {
		super(links);
	}

	EntryPoint() {
	}

	public URI getCreatePrintMessageUri() {
		Link printMessageUri = getLinkByRelationName(CREATE_PRINT_MESSAGE);
		return printMessageUri == null ? null : printMessageUri.getUri();
	}

	public URI getCreateMessageUri() {
		return getLinkByRelationName(CREATE_MESSAGE).getUri();
	}

	public URI getSearchUri() {
		return getLinkByRelationName(SEARCH).getUri();
	}

	public URI getAutocompleteUri() {
		return getLinkByRelationName(AUTOCOMPLETE).getUri();
	}

	@XmlElement(name = "link")
	protected List<Link> getLink() {
		return links;
	}

	protected void setLink(final List<Link> links) {
		this.links = links;
	}
}
