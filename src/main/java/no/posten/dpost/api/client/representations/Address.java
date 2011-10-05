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
package no.posten.dpost.api.client.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Address {

	private String street;
	private String houseNumber;
	private String houseLetter;
	private String additionalAddressLine;
	private String zipCode;
	private String city;

	public Address(final String street, final String houseNumber, final String houseLetter, final String additionalAddressLine,
			final String zipCode, final String city) {
		this.street = street;
		this.houseNumber = houseNumber;
		this.houseLetter = houseLetter;
		this.additionalAddressLine = additionalAddressLine;
		this.zipCode = zipCode;
		this.city = city;
	}

	Address() {
	}

	public String getStreet() {
		return street;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public String getHouseLetter() {
		return houseLetter;
	}

	public String getAdditionalAddressLine() {
		return additionalAddressLine;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getCity() {
		return city;
	}
}
