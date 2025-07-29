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
package no.digipost.api.client.representations.shareddocuments;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "shared-document-origin", propOrder = {
        "privatePerson",
        "organisation"
})
public class SharedDocumentOrigin {

    @XmlElement(name = "private-person", nillable = false)
    protected PrivatePersonOrigin privatePerson;
    @XmlElement(name = "organisation", nillable = false)
    protected OrganisationOrigin organisation;

    public SharedDocumentOrigin() {
    }

    public SharedDocumentOrigin(PrivatePersonOrigin privatePerson, OrganisationOrigin organisation) {
        this.privatePerson = privatePerson;
        this.organisation = organisation;
    }

    public PrivatePersonOrigin getPrivatePerson() {
        return privatePerson;
    }

    public void setPrivatePerson(PrivatePersonOrigin privatePerson) {
        this.privatePerson = privatePerson;
    }

    public OrganisationOrigin getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationOrigin organisation) {
        this.organisation = organisation;
    }

    @Override
    public String toString() {
        return "SharedDocumentOrigin{" +
                "privatePerson=" + privatePerson +
                ", organisation=" + organisation +
                '}';
    }
}
