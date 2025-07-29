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

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "email-notification", propOrder = {
        "emailAddress",
        "subject",
        "text",
        "ats"
})
public class EmailNotification {
    @XmlElement(name = "email-address")
    public final String emailAddress;
    @XmlElement(name = "subject")
    public final String subject;
    @XmlElement(name = "text")
    public final String text;
    @XmlElement(name = "at", nillable = false)
    public final List<ListedTime> ats;

    EmailNotification() {
        this(null, null, null, null);
    }

    public EmailNotification(String emailAddress, String subject, String text, List<ListedTime> ats) {
        this.emailAddress = emailAddress;
        this.subject = subject;
        this.text = text;
        this.ats = ats;
    }
}
