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

@XmlAccessorType(XmlAccessType.FIELD)
public class Recipient extends Representation {

	private String firstName;
	private String middleName;
	private String lastName;
	@XmlElement(name = "digipostAddress")
	private String digipostAddress;
	@XmlElement(name = "address")
	private final List<Address> addresses;

	public Recipient(final String firstName, final String middleName, final String lastName, final String digipostAddress,
			final List<Address> addresses, final Link... links) {
		super(links);
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.digipostAddress = digipostAddress;
		this.addresses = addresses;
	}

	Recipient() {
		addresses = new ArrayList<Address>();
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getDigipostAddress() {
		return digipostAddress;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public static class Builder {
		private final String firstName;
		private final String middleName;
		private final String lastName;
		private final String digipostAddress;
		private final List<Address> addresses;
		private final List<Link> links;

		public Builder(final String firstName, final String middleName, final String lastName, final String digipostAddress) {
			this.lastName = lastName;
			this.middleName = middleName;
			this.firstName = firstName;
			this.digipostAddress = digipostAddress;
			addresses = new ArrayList<Address>();
			links = new ArrayList<Link>();
		}

		public Builder address(final Address address) {
			addresses.add(address);
			return this;
		}

		public Builder link(final Link link) {
			links.add(link);
			return this;
		}

		public Recipient build() {
			return new Recipient(firstName, middleName, lastName, digipostAddress, addresses, links.toArray(new Link[links.size()]));
		}
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
