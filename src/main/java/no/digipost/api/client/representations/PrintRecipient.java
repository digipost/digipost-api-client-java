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

import static org.apache.commons.lang.StringUtils.trimToEmpty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "print-recipient", propOrder = { "name", "addressline1", "addressline2", "zipCode", "city", "country" })
public class PrintRecipient {

	public PrintRecipient() {
	}

	public PrintRecipient(final String name, final String zipCode, final String city) {
		this.name = name;
		this.zipCode = zipCode;
		this.city = city;
	}

	@XmlElement(required = true)
	protected String name;
	protected String addressline1;
	protected String addressline2;
	@XmlElement(required = true)
	protected String zipCode;
	@XmlElement(required = true)
	protected String city;
	protected String country;

	public String getName() {
		return name;
	}

	public void setName(final String value) {
		name = value;
	}

	public String getAddressline1() {
		return addressline1;
	}

	public void setAddressline1(final String value) {
		addressline1 = value;
	}

	public String getAddressline2() {
		return addressline2;
	}

	public void setAddressline2(final String value) {
		addressline2 = value;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(final String value) {
		zipCode = value;
	}

	public String getCity() {
		return city;
	}

	public void setCity(final String value) {
		city = value;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(final String value) {
		country = value;
	}

	public boolean isSameRecipientAs(final PrintRecipient other) {
		return trimEquals(name, other.name) && trimEquals(city, other.city) && trimEquals(zipCode, other.zipCode);
	}

	private boolean trimEquals(final String first, final String second) {
		return trimToEmpty(first).equals(trimToEmpty(second));
	}

}
