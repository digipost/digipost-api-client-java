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
package no.digipost.api.client.representations.accounts;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "phone-number")
public class PhoneNumber  {
    @XmlElement(name = "country-code")
	private final String countryCode;
    @XmlElement(name = "phone-number")
	private final String phoneNumber;

    /**
     * Creates a phone number consisting of a country calling code and a phone number: +47 12345678
     *
     * @param countryCode ITU-T country calling code (without + prefix) for the phoneNumber ie. 47 for Norway
     * @param phoneNumber Phone number excluding country calling code
     */
	public PhoneNumber(String countryCode, String phoneNumber) {
		this.countryCode = countryCode;
		this.phoneNumber = phoneNumber;
	}

	//jaxb
	private PhoneNumber() {
	    this(null, null);
    }

	public String getCountryCode() {
		return countryCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	@Override
	public String toString() {
		return "PhoneNumber{" +
			"countryCode='" + countryCode + '\'' +
			", phoneNumber='" + phoneNumber + '\'' +
			'}';
	}
}
