/*
 * Copyright (C) Posten Bring AS
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Recipient extends Representation {

    private String firstname;
    private String middlename;
    private String lastname;
    @XmlElement(name = "digipost-address", required = true)
    private String digipostAddress;
    @XmlElement(name = "mobile-number")
    protected String mobileNumber;
    @XmlElement(name = "organisation-number")
    private String organisationNumber;
    @XmlElement(name = "organisation-name")
    private String organisationName;
    @XmlElement(name = "address")
    private final List<Address> addresses;

    public Recipient(final String firstName, final String middleName, final String lastName, final String digipostAddress,
            final List<Address> addresses, final Link... links) {
        super(links);
        firstname = firstName;
        middlename = middleName;
        lastname = lastName;
        this.digipostAddress = digipostAddress;
        this.addresses = addresses;
    }

    Recipient() {
        addresses = new ArrayList<Address>();
    }

    public String getFirstname() {
        return firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public String getDigipostAddress() {
        return digipostAddress;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getOrganisationNumber() {
        return organisationNumber;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public Link getSelfLink() {
        return getLinkByRelationName(Relation.SELF);
    }

    @XmlElement(name = "link")
    protected List<Link> getLinks() {
        return links;
    }

    protected void setLinks(final List<Link> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "Recipient{" +
                "firstname='" + firstname + '\'' +
                ", middlename='" + middlename + '\'' +
                ", lastname='" + lastname + '\'' +
                ", digipostAddress='" + digipostAddress + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", organisationNumber='" + organisationNumber + '\'' +
                ", organisationName='" + organisationName + '\'' +
                ", addresses=" + addresses +
                ", links=" + links +
                '}';
    }
}
