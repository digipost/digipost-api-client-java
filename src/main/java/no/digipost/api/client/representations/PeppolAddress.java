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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "peppol-address", propOrder = {
        "schemeID",
        "endpointID",
})
public class PeppolAddress {
    @XmlElement(name = "schemeID", required = true)
    protected String schemeID;
    @XmlElement(name = "endpointID", required = true)
    protected String endpointID;

    PeppolAddress() {
    }

    public PeppolAddress(String address) {
        this.schemeID = address.substring(0, 4);
        this.endpointID = address.substring(5);
    }
    
    public PeppolAddress(String schemeID, String endpointID) {
        this.schemeID = schemeID;
        this.endpointID = endpointID;
    }

    public String getSchemeID() {
        return schemeID;
    }

    public String getEndpointID() {
        return endpointID;
    }

    @Override
    public String toString() {
        return schemeID + ":" + endpointID;
    }
}
