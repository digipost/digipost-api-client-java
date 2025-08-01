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
package no.digipost.api.client.representations.accounts;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user-information")
public class UserInformation {

    @XmlElement(name = "national-identity-number", required = true)
    private final NationalIdentityNumber nationalIdentityNumber;
    @XmlElement(name = "phone-number", required = true)
    private final PhoneNumber phoneNumber;
    @XmlElement(name = "email-address", required = true)
    private final EmailAddress emailAddress;

    public UserInformation(NationalIdentityNumber nationalIdentityNumber, PhoneNumber phoneNumber, EmailAddress emailAddress) {
        this.nationalIdentityNumber = nationalIdentityNumber;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    @SuppressWarnings("unused")
    private UserInformation() {
        this(null, null, null);
    }

    @Override
    public String toString() {
        return "UserInformation{" +
                "nationalIdentityNumber=" + nationalIdentityNumber +
                ", phoneNumber=" + phoneNumber +
                ", emailAddress=" + emailAddress +
                '}';
    }

    public NationalIdentityNumber getNationalIdentityNumber() {
        return nationalIdentityNumber;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public EmailAddress getEmailAddress() {
        return emailAddress;
    }
}
