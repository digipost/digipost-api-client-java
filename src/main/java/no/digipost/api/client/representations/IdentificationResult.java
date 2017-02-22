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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identification-result", propOrder = {
        "result",
        "invalidReason",
        "unidentifiedReason",
        "personAlias",
        "digipostAddress"
})
@XmlRootElement(name = "identification-result")
public class IdentificationResult {

    @XmlElement(required = true)
    protected IdentificationResultCode result;
    @XmlElement(name = "invalid-reason")
    protected InvalidReason invalidReason;
    @XmlElement(name = "unidentified-reason")
    protected UnidentifiedReason unidentifiedReason;
    @XmlElement(name = "person-alias")
    protected String personAlias;
    @XmlElement(name = "digipost-address")
    protected String digipostAddress;

    public IdentificationResult() {
    }

    public IdentificationResultCode getResult() {
        return result;
    }

    public InvalidReason getInvalidReason() {
        return invalidReason;
    }

    public UnidentifiedReason getUnidentifiedReason() {
        return unidentifiedReason;
    }

    public String getPersonAlias() {
        return personAlias;
    }

    public String getDigipostAddress() {
        return digipostAddress;
    }

    public static IdentificationResult digipost(String digipostAddress) {
        IdentificationResult result = new IdentificationResult();
        result.result = IdentificationResultCode.DIGIPOST;
        result.digipostAddress = digipostAddress;
        return result;
    }

    @Override
    public String toString() {
        return "IdentificationResult{" +
                "result=" + result +
                ", invalidReason=" + invalidReason +
                ", unidentifiedReason=" + unidentifiedReason +
                ", personAlias='" + personAlias + '\'' +
                ", digipostAddress='" + digipostAddress + '\'' +
                '}';
    }
}
