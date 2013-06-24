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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import no.digipost.api.client.representations.xml.DateXmlAdapter;
import org.joda.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "name-and-address", propOrder = {
    "fullname",
    "addressline1",
    "addressline2",
    "postalcode",
    "city",
	"birthDate",
	"phoneNumber",
	"emailAddress"
})
public class NameAndAddress {
    @XmlElement(required = true)
    protected String fullname;
    @XmlElement(required = true)
    protected String addressline1;
    @XmlElement(nillable = false)
    protected String addressline2;
    @XmlElement(required = true)
    protected String postalcode;
    @XmlElement(required = true)
    protected String city;
	@XmlElement(name = "birth-date", type = String.class, nillable = false)
	@XmlJavaTypeAdapter(DateXmlAdapter.class)
	@XmlSchemaType(name = "date")
	protected LocalDate birthDate;
	@XmlElement(name = "phone-number", nillable = false)
	protected String phoneNumber;
	@XmlElement(name = "email-address", nillable = false)
	protected String emailAddress;

	NameAndAddress() {
	}

	public NameAndAddress(final String fullname, final String addressline1, final String addressline2, final String postalcode, final String city) {
		this.fullname = fullname;
		this.addressline1 = addressline1;
		this.addressline2 = addressline2;
		this.postalcode = postalcode;
		this.city = city;
	}

	public NameAndAddress(final String fullname, final String addressline1, final String addressline2, final String postalcode, final String city,
						  final LocalDate birthDate, final String phoneNumber, final String emailAddress) {
		this.fullname = fullname;
		this.addressline1 = addressline1;
		this.addressline2 = addressline2;
		this.postalcode = postalcode;
		this.city = city;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}

	@Override
	public String toString() {
		return "NameAndAddress{" +
				"fullname='" + fullname + '\'' +
				", addressline1='" + addressline1 + '\'' +
				", addressline2='" + addressline2 + '\'' +
				", postalcode='" + postalcode + '\'' +
				", city='" + city + '\'' +
				", birthDate=" + birthDate +
				", phoneNumber='" + phoneNumber + '\'' +
				", emailAddress='" + emailAddress + '\'' +
				'}';
	}
}
