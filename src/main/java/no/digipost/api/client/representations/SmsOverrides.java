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
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sms-overrides", propOrder = {
        "smsMobileNumber",
        "text"
})
public class SmsOverrides {

    @XmlElement(name = "sms-mobile-number")
    public final String smsMobileNumber;
    @XmlElement(name = "text")
    public final String text;

    SmsOverrides() {
        this(null, null);
    }

    public SmsOverrides(final String smsMobileNumber, final String text) {
        this.smsMobileNumber = smsMobileNumber;
        this.text = text;
    }
}
