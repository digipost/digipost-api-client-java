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

import jakarta.xml.bind.annotation.*;
import java.util.Objects;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identification", propOrder = {
        "nameAndAddress",
        "digipostAddress",
        "bankAccountNumber",
        "organisationNumber",
        "personalIdentificationNumber"
})
@XmlRootElement(name = "identification")
public class Identification {

    @XmlAttribute(name = "include-personalias-for-digipost-user")
    protected boolean includePersonaliasForDigipostUser;

    @XmlElement(name = "name-and-address", nillable = false)
    protected NameAndAddress nameAndAddress;
    @XmlElement(name = "digipost-address", nillable = false)
    protected String digipostAddress;
    @XmlElement(name = "bank-account-number", nillable = false)
    protected String bankAccountNumber;
    @XmlElement(name = "personal-identification-number", nillable = false)
    protected String personalIdentificationNumber;
    @XmlElement(name = "organisation-number", nillable = false)
    protected String organisationNumber;

    public Identification(final NameAndAddress nameAndAddress) {
        this(nameAndAddress, false);
    }

    public Identification(final NameAndAddress nameAndAddress, boolean includePersonaliasForDigipostUser) {
        this.nameAndAddress = nameAndAddress;
        this.includePersonaliasForDigipostUser = includePersonaliasForDigipostUser;
    }

    public Identification(final DigipostAddress digipostAddress) {
        this(digipostAddress, false);
    }
    public Identification(final DigipostAddress digipostAddress, boolean includePersonaliasForDigipostUser) {
        this.includePersonaliasForDigipostUser = includePersonaliasForDigipostUser;
        this.digipostAddress = digipostAddress.asString();
    }

    public Identification(final PersonalIdentificationNumber personalIdentificationNumber) {
        this(personalIdentificationNumber, false);
    }
    public Identification(final PersonalIdentificationNumber personalIdentificationNumber, boolean includePersonaliasForDigipostUser) {
        this.includePersonaliasForDigipostUser = includePersonaliasForDigipostUser;
        this.personalIdentificationNumber = personalIdentificationNumber.asString();
    }

    public Identification(final OrganisationNumber organisationNumber) {
        this.includePersonaliasForDigipostUser = false;
        this.organisationNumber = organisationNumber.asString();
    }

    public Identification(final BankAccountNumber bankAccountNumber) {
        this.includePersonaliasForDigipostUser = false;
        this.bankAccountNumber = bankAccountNumber.asString();
    }

    //JAXB
    public Identification() {

    }

    public NameAndAddress getNameAndAddress() {
        return nameAndAddress;
    }

    public String getDigipostAddress() {
        return digipostAddress;
    }

    public String getPersonalIdentificationNumber() {
        return personalIdentificationNumber;
    }

    @Override
    public String toString() {
        if (digipostAddress != null) {
            return digipostAddress;
        } else if (bankAccountNumber != null) {
            return bankAccountNumber;
        } else if (personalIdentificationNumber != null) {
            return personalIdentificationNumber;
        } else if (nameAndAddress != null) {
            return nameAndAddress.toString();
        } else {
            return "empty";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Identification) {
            Identification that = (Identification) obj;
            return Objects.equals(this.digipostAddress, that.digipostAddress) &&
                    Objects.equals(this.includePersonaliasForDigipostUser, that.includePersonaliasForDigipostUser) &&
                    Objects.equals(this.nameAndAddress, that.nameAndAddress) &&
                    Objects.equals(this.personalIdentificationNumber, that.personalIdentificationNumber) &&
                    Objects.equals(this.organisationNumber, that.organisationNumber) &&
                    Objects.equals(this.bankAccountNumber, that.bankAccountNumber);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(digipostAddress, includePersonaliasForDigipostUser, nameAndAddress, personalIdentificationNumber, organisationNumber, bankAccountNumber);
    }
}

