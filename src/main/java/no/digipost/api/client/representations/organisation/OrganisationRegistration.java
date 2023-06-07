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
package no.digipost.api.client.representations.organisation;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisation-registration")
public class OrganisationRegistration {

    @XmlElement(required = true)
    private String name;
    @XmlElement(name = "organisation-number", required = true)
    private String organisationNumber;
    @XmlElement(name = "organisation-part")
    private String organisationPart;
    @XmlElement(name = "activate-mailbox", required = true)
    private boolean activateMailbox;
    @XmlElement(name = "phone-number", required = true)
    private String phoneNumber;
    @XmlElement(name = "email-address", required = true)
    private String emailAddress;
    @XmlElement(required = true)
    private String language;
    @XmlElement(required = true)
    private OrganisationRegistrationTrustee administrator;
    @XmlElement(required = true)
    private OrganisationRegistrationAddressInformation addressInformation;

    public OrganisationRegistration() {}

	public OrganisationRegistration(String name, String organisationNumber, boolean activateMailbox, String phoneNumber, String emailAddress, Language language, OrganisationRegistrationTrustee administrator, OrganisationRegistrationAddressInformation addressInformation) {
		this.name = name;
		this.organisationNumber = organisationNumber;
		this.activateMailbox = activateMailbox;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		this.language = language.getKode();
		this.administrator = administrator;
		this.addressInformation = addressInformation;
	}

	@Override
	public String toString() {
		return "OrganisationRegistration{" +
			"name=" + name +
			", organisationNumber=" + organisationNumber +
            ", organisationPart=" + organisationPart +
			", phoneNumber=" + phoneNumber +
			", emailAddress=" + emailAddress +
			", activateMailbox=" + activateMailbox +
			", administrator=" + administrator +
            ", language=" + language +
            ", addressInformation=" + addressInformation +
            '}';
	}

	public String getLanguage() {
		return language;
	}

    public void setOrganisationPart(String organisationPart) {
        this.organisationPart = organisationPart;
    }
}
