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
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-recipient", propOrder = {
    "nameAndAddress",
    "digipostAddress",
    "personalIdentificationNumber",
    "organisationNumber",
    "printDetails",
	"notifications"
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
	@XmlElement(name = "notifications")
	protected Notifications notifications;

	public MessageRecipient() {}

	public MessageRecipient(final PersonalIdentificationNumber id) {
		this.personalIdentificationNumber = id.asString();
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

	public PrintDetails getPrintDetails() {
		return printDetails;
	}

	public boolean isDirectPrint() {
		return printDetails != null && digipostAddress == null && personalIdentificationNumber == null && nameAndAddress == null;
	}
}
