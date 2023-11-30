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
package no.digipost.api.client.representations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "email-notification-failed-metadata")
public class EmailNotificationFailedMetadata extends EventMetadata {

    @XmlAttribute(name = "email-address")
    public final String emailAddress;
    @XmlAttribute(name = "error-code")
    public final String errorCode;

    public EmailNotificationFailedMetadata() {
        this(null, null);
    }

    public EmailNotificationFailedMetadata(String emailAddress, String errorCode) {
        this.emailAddress = emailAddress;
        this.errorCode = errorCode;
    }
}
