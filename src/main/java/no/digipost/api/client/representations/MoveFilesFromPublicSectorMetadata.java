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

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "move-files-from-public-sector-metadata")
public class MoveFilesFromPublicSectorMetadata extends EventMetadata {

    @XmlAttribute(name = "opened")
    public final Boolean opened;
    @XmlAttribute(name = "delivery-time", required = true)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    public final ZonedDateTime deliveryTime;
    @XmlAttribute(name = "subject")
    public final String subject;
    @XmlAttribute(name = "sensitivity-level")
    public final SensitivityLevel sensitivityLevel;
    @XmlAttribute(name = "authentication-level")
    public final AuthenticationLevel authenticationLevel;
    @XmlAttribute(name = "destination-mailbox")
    public final String destinationMailbox;
    @XmlAttribute(name = "destination-mailbox-address")
    public final String destinationMailboxAddress;

    @XmlElement(name = "document")
    public final List<DocumentMetadata> documents;
    @XmlElement(name = "x509Certificate")
    public final String x509Certificate;

    public MoveFilesFromPublicSectorMetadata() {
        this(null, null, null, null, null, null, null, null, new ArrayList<DocumentMetadata>());
    }

    public MoveFilesFromPublicSectorMetadata(Boolean opened, ZonedDateTime deliveryTime, String subject, SensitivityLevel sensitivityLevel,
                                             AuthenticationLevel authenticationLevel, String x509Certificate, String destinationMailbox,
                                             String destinationMailboxAddress, List<DocumentMetadata> documents) {
        this.opened = opened;
        this.deliveryTime = deliveryTime;
        this.subject = subject;
        this.sensitivityLevel = sensitivityLevel;
        this.authenticationLevel = authenticationLevel;
        this.x509Certificate = x509Certificate;
        this.destinationMailbox = destinationMailbox;
        this.destinationMailboxAddress = destinationMailboxAddress;
        this.documents = documents;
    }
}
