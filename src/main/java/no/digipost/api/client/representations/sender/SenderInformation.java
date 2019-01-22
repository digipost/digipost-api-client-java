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

import no.digipost.api.client.SenderId;
import no.digipost.print.validate.PdfValidationSettings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_FONTS;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_MARGINS_LEFT;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_NEGATIVE_BLEED;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_PAGEAMOUNT;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_PDFVERSION;
import static no.digipost.api.client.representations.sender.SenderFeatureName.PRINTVALIDATION_POSITIVE_BLEED;
import static no.digipost.print.validate.PdfValidationSettings.DEFAULT_NEGATIVE_BLEED_MM;
import static no.digipost.print.validate.PdfValidationSettings.DEFAULT_POSITIVE_BLEED_MM;

/**
 * Informasjon om en avsender. Bruk
 * {@link #is(SenderStatus) is(}{@link SenderStatus#VALID_SENDER VALID_SENDER)}
 * for å avgjøre om avsenderen er gyldig, og eksempelvis
 * {@link #hasEnabled(SenderFeatureName) hasEnabled(}{@link SenderFeatureName#DIGIPOST_DELIVERY DIGIPOST_DELIVERY)}
 * for å sjekke om du kan sende post fra avsenderen med REST-APIet til Digipost.
 *
 * @see SenderStatus
 * @see SenderFeatureName
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
    private List<SenderFeature> supportedFeatures;

    public SenderInformation() { }

    public SenderInformation(SenderId senderId, SenderStatus status, List<SenderFeature> supportedFeatures) {
        this.senderId = senderId != null ? senderId.value() : null;
        this.status = status;
        this.supportedFeatures = supportedFeatures == null || supportedFeatures.isEmpty() ? null : supportedFeatures;
    }


    public Long getSenderId() {
        return senderId;
    }

    public boolean is(SenderStatus status) {
        return this.status == status;
    }

    public List<SenderFeature> getSupportedFeatures() {
        return supportedFeatures;
    }

    public boolean hasEnabled(SenderFeatureName featureName) {
        return get(featureName) != null;
    }

    public SenderFeature get(SenderFeatureName featureName) {
        for (SenderFeature feature : supportedFeatures ){
            if (featureName.equals(feature.getName())) {
                return feature;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(status.toString());
        if (status != SenderStatus.NO_INFO_AVAILABLE) {
            s.append(" - id: ").append(senderId)
             .append(", supported features: ")
             .append(supportedFeatures.stream().map(Object::toString).collect(joining(", ")));
        }
        return s.toString();
    }

    public PdfValidationSettings getPdfValidationSettings() {
        SenderFeature positiveBleed = get(PRINTVALIDATION_POSITIVE_BLEED);
        SenderFeature negativeBleed = get(PRINTVALIDATION_NEGATIVE_BLEED);
        return new PdfValidationSettings(
                hasEnabled(PRINTVALIDATION_MARGINS_LEFT),
                hasEnabled(PRINTVALIDATION_FONTS),
                hasEnabled(PRINTVALIDATION_PAGEAMOUNT),
                hasEnabled(PRINTVALIDATION_PDFVERSION),
                positiveBleed != null ? positiveBleed.getIntParam() : DEFAULT_POSITIVE_BLEED_MM,
                negativeBleed != null ? negativeBleed.getIntParam() : DEFAULT_NEGATIVE_BLEED_MM
        );
    }
}
