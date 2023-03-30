/*
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

import no.digipost.api.client.representations.xml.DateXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.Objects;

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
public final class NameAndAddress {
    @XmlElement(required = true)
    private String fullname;
    @XmlElement(required = true)
    private String addressline1;
    @XmlElement(nillable = false)
    private String addressline2;
    @XmlElement(required = true)
    private String postalcode;
    @XmlElement(required = true)
    private String city;
    @XmlElement(name = "birth-date", type = String.class, nillable = false)
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    @XmlSchemaType(name = "date")
    private LocalDate birthDate;
    @XmlElement(name = "phone-number", nillable = false)
    private String phoneNumber;
    @XmlElement(name = "email-address", nillable = false)
    private String emailAddress;

    NameAndAddress() {
    }

    public NameAndAddress(String fullname, String addressline1, String addressline2, String postalcode, String city) {
        this.fullname = fullname;
        this.addressline1 = addressline1;
        this.addressline2 = addressline2;
        this.postalcode = postalcode;
        this.city = city;
    }

    public NameAndAddress(String fullname, String addressline1, String addressline2, String postalcode, String city,
                          LocalDate birthDate, String phoneNumber, String emailAddress) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NameAndAddress) {
            NameAndAddress that = (NameAndAddress) obj;
            return Objects.equals(this.addressline1, that.addressline1) &&
                    Objects.equals(this.addressline2, that.addressline2) &&
                    Objects.equals(this.postalcode, that.postalcode) &&
                    Objects.equals(this.city, that.city) &&
                    Objects.equals(this.fullname, that.fullname) &&
                    Objects.equals(this.birthDate, that.birthDate) &&
                    Objects.equals(this.emailAddress, that.emailAddress) &&
                    Objects.equals(this.phoneNumber, that.phoneNumber);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressline1, addressline2, postalcode, city, fullname, birthDate, emailAddress, phoneNumber);
    }
}
