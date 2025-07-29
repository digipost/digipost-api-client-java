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

import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sender-organization", propOrder = {
        "organizationId",
        "partId"
})
public final class SenderOrganization {

    @XmlElement(name = "organization-id", nillable = false)
    public final String organizationId;

    @XmlElement(name = "part-id")
    public final String partId;


    @SuppressWarnings("unused")
    private SenderOrganization() {
        this(null, null);
    }

    public SenderOrganization(@XmlElement(name = "organization-id", nillable = false) String organizationId, @XmlElement(name = "part-id") String partId) {
        this.organizationId = organizationId;
        this.partId = partId;
    }

    @Override
    public String toString() {
        return organizationId + (partId == null ? "" : ":" + partId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SenderOrganization) {
            SenderOrganization that = (SenderOrganization) obj;
            return Objects.equals(this.organizationId, that.organizationId)
                && Objects.equals(this.partId, that.partId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, partId);
    }

}
