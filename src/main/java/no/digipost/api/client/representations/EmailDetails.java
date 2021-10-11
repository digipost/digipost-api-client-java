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
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "email-details", propOrder = {
    "emailAddresses"
})
public class EmailDetails
{

    @XmlElement(name = "email-address", required = true)
    protected List<String> emailAddresses;

    public EmailDetails() {
    }

    public EmailDetails(String... emailAddresses) {
       this(Arrays.asList(emailAddresses));
    }

    public EmailDetails(List<String> emailAddresses) {
        this.emailAddresses = emailAddresses;
    }

    public List<String> getEmailAddress() {
        return emailAddresses;
    }


    public void setEmailAddress(String value) {
        this.emailAddresses = Collections.singletonList(value);
    }

    public void setEmailAddress(List<String> value) {
        this.emailAddresses = value;
    }

}
