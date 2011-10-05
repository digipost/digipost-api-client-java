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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Autocomplete extends Representation {

	@XmlElement(name = "suggestion")
	private final List<Suggestion> suggestions;

	public Autocomplete(final List<Suggestion> suggestions, final Link... links) {
		super(links);
		this.suggestions = suggestions;
	}

	Autocomplete() {
		suggestions = new ArrayList<Suggestion>();
	}

	public List<Suggestion> getSuggestions() {
		return suggestions;
	}

	public Link getSelfLink() {
		return getLinkByRelationName(Relation.SELF);
	}

	@XmlElement(name = "link")
	protected List<Link> getLink() {
		return links;
	}

	protected void setLink(final List<Link> links) {
		this.links = links;
	}
}
