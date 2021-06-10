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

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-recipient", propOrder = {
        "nameAndAddress",
        "digipostAddress",
        "personalIdentificationNumber",
        "organisationNumber",
        "printDetails",
        "bankAccountNumber",
        "emailDetails"
})
public class MessageRecipient {

    @XmlElement(name = "name-and-address", nillable = false)
    protected NameAndAddress nameAndAddress;
    @XmlElement(name = "digipost-address", nillable = false)
    protected String digipostAddress;
    @XmlElement(name = "personal-identification-number", nillable = false)
    protected String personalIdentificationNumber;
    @XmlElement(name = "organisation-number", nillable = false)
    protected String organisationNumber;
    @XmlElement(name = "print-details", nillable = false)
    protected PrintDetails printDetails;
    @XmlElement(name = "bank-account-number", nillable = false)
    protected String bankAccountNumber;
    @XmlElement(name = "email-details", nillable = false)
    protected EmailDetails emailDetails;

    public MessageRecipient() {
    }

    MessageRecipient(NameAndAddress nameAndAddress, String digipostAddress, String personalIdentificationNumber,
                            String organisationNumber, PrintDetails printDetails, String bankAccountNumber){
        this.nameAndAddress = nameAndAddress;
        this.digipostAddress = digipostAddress;
        this.personalIdentificationNumber = personalIdentificationNumber;
        this.organisationNumber = organisationNumber;
        this.printDetails = printDetails;
        this.bankAccountNumber = bankAccountNumber;
    }

    public MessageRecipient(final PersonalIdentificationNumber id) {
        this.personalIdentificationNumber = id.asString();
    }

    public MessageRecipient(final BankAccountNumber bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber.asString();
    }

    public MessageRecipient(final DigipostAddress digipostAddress) {
        this.digipostAddress = digipostAddress.asString();
    }

    public MessageRecipient(final OrganisationNumber organisationNumber) {
        this.organisationNumber = organisationNumber.asString();
    }

    public MessageRecipient(final NameAndAddress nameAndAddress) {
        this.nameAndAddress = nameAndAddress;
    }

    public MessageRecipient(final PersonalIdentificationNumber id, final PrintDetails printDetails) {
        this(id);
        this.printDetails = printDetails;
    }

    public MessageRecipient(final DigipostAddress digipostAddress, final PrintDetails printDetails) {
        this(digipostAddress);
        this.printDetails = printDetails;
    }

    public MessageRecipient(final NameAndAddress nameAndAddress, final PrintDetails printDetails) {
        this(nameAndAddress);
        this.printDetails = printDetails;
    }

    public MessageRecipient(final OrganisationNumber organisationNumber, final PrintDetails printDetails) {
        this(organisationNumber);
        this.printDetails = printDetails;
    }

    public MessageRecipient(final EmailDetails emailDetails) {
        this.emailDetails = emailDetails;
    }

    public MessageRecipient(final PrintDetails printDetails) {
        this.printDetails = printDetails;
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

    public String getOrganisationNumber() {
        return organisationNumber;
    }

    public PrintDetails getPrintDetails() {
        return printDetails;
    }

    public boolean isDirectPrint() {
        return hasPrintDetails() && !hasDigipostIdentification();
    }

    public boolean hasPrintDetails() {
        return printDetails != null;
    }

    public boolean hasDigipostIdentification() {
        return digipostAddress != null || personalIdentificationNumber != null || nameAndAddress != null || organisationNumber != null;
    }

    public Identification toIdentification() {
        if (isDirectPrint()) {
            throw new IllegalStateException("MessageRecipient mangler identifikasjonsdetaljer.");
        }

        if (digipostAddress != null) {
            return new Identification(new DigipostAddress(digipostAddress));
        } else if (nameAndAddress != null) {
            return new Identification(nameAndAddress);
        } else if (organisationNumber != null) {
            return new Identification(new OrganisationNumber(organisationNumber));
        } else if (personalIdentificationNumber != null) {
            return new Identification(new PersonalIdentificationNumber(personalIdentificationNumber));
        } else {
            throw new DigipostClientException(ErrorCode.CLIENT_ERROR, "Ukjent identifikationstype.");
        }
    }
}
