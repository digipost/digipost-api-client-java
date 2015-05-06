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
package no.digipost.api.client.representations.sender;

import no.motif.types.Elements;

import javax.xml.bind.annotation.*;

import java.util.List;

import static no.digipost.api.client.representations.sender.SenderFeature.getIdentificator;
import static no.motif.Base.equalTo;
import static no.motif.Iterate.on;

/**
 * Informasjon om en avsender. Bruk
 * {@link #is(SenderStatus) is(}{@link SenderStatus#VALID_SENDER VALID_SENDER)}
 * for å avgjøre om avsenderen er gyldig, og eksempelvis
 * {@link #hasEnabled(SenderFeature) hasEnabled(}{@link SenderFeature#DIGIPOST_DELIVERY DIGIPOST_DELIVERY)}
 * for å sjekke om du kan sende post fra avsenderen med REST-APIet til Digipost.
 *
 * @see SenderStatus
 * @see SenderFeature
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sender-information", propOrder = {
    "senderId",
    "status",
    "supportedFeatures"
})
@XmlRootElement(name = "sender-information")
public class SenderInformation
{

    @XmlElement(name = "sender-id", nillable = false)
    private Long senderId;

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    private SenderStatus status;

    @XmlElementWrapper(name = "supported-features")
    @XmlElement(name = "feature", nillable = false)
    private List<String> supportedFeatures;

    public SenderInformation() { }

    public SenderInformation(Long senderId, SenderStatus status, List<SenderFeature> supportedFeatures) {
    	this.senderId = senderId;
    	this.status = status;
    	this.supportedFeatures = supportedFeatures == null || supportedFeatures.isEmpty() ? null : on(supportedFeatures).map(getIdentificator).collect();
    }


    public Long getSenderId() {
        return senderId;
    }

    public boolean is(SenderStatus status) {
        return this.status == status;
    }

    public Elements<SenderFeature> getSupportedFeatures() {
    	return on(supportedFeatures).map(SenderFeature.toSenderFeature);
    }

    public boolean hasEnabled(SenderFeature feature) {
    	return getSupportedFeatures().exists(equalTo(feature));
    }

    @Override
    public String toString() {
    	StringBuilder s = new StringBuilder(status.toString());
    	if (status != SenderStatus.NO_INFO_AVAILABLE) {
    		s.append(" - id: ").append(senderId)
			 .append(", supported features: ").append(on(supportedFeatures).join(", "));
    	}
    	return s.toString();
    }
}