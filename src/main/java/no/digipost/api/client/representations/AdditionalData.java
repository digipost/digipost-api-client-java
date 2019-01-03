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

import no.digipost.api.datatypes.DataType;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "additional-data", propOrder = {
        "dataType",
        "senderOrganization",
        "senderId"
})
@XmlRootElement(name = "additional-data")
public class AdditionalData {

    @XmlElement(name = "data-type", required = true)
    protected DataTypeHolder dataType;
    @XmlElement(name = "sender-id", nillable = false)
    protected Long senderId;
    @XmlElement(name = "sender-organization", nillable = false)
    protected SenderOrganization senderOrganization;

    AdditionalData() {
        this(null, null, null);
    }

    public static class Builder {
        private DataType data;
        private SenderOrganization senderOrganization = null;
        private Long senderId = null;

        private Builder(DataType data) {
            this.data = data;
        }

        public static Builder newAdditionalData(DataType data) {
            return new Builder(data);
        }

        public Builder setSenderOrganization(SenderOrganization senderOrganization) {
            this.senderOrganization = senderOrganization;
            return this;
        }

        public Builder setSenderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public AdditionalData build() {
            if (senderId != null && senderOrganization != null) {
                throw new IllegalStateException("You can't set both senderId *and* senderOrganization.");
            }
            return new AdditionalData(data, senderOrganization, senderId);
        }
    }

    private AdditionalData(DataType data, SenderOrganization senderOrganization, Long senderId) {
        this.dataType = new DataTypeHolder(data);
        this.senderOrganization = senderOrganization;
        this.senderId = senderId;
    }
}
