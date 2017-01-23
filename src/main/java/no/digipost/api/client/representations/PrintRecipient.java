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

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "print-recipient", propOrder = { "name", "norwegianAddress", "foreignAddress" })
public class PrintRecipient {

    public PrintRecipient() {
    }

    public PrintRecipient(final String name, final NorwegianAddress norwegianAddress) {
        this.name = name;
        this.norwegianAddress = norwegianAddress;
    }

    public PrintRecipient(final String name, final ForeignAddress foreignAddress) {
        this.name = name;
        this.foreignAddress = foreignAddress;
    }

    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "norwegian-address")
    protected NorwegianAddress norwegianAddress;
    @XmlElement(name = "foreign-address")
    protected ForeignAddress foreignAddress;

    public String getName() {
        return name;
    }

    public NorwegianAddress getNorwegianAddress() {
        return norwegianAddress;
    }

    public ForeignAddress getForeignAddress() {
        return foreignAddress;
    }

    public void setName(final String value) {
        name = value;
    }

    public boolean isSameRecipientAs(final PrintRecipient other) {
        boolean nameMatches = trimEquals(name, other.name);
        if (norwegianAddress != null && other.norwegianAddress != null) {
            return nameMatches && norwegianAddress.isSameAddressAs(other.norwegianAddress);
        } else if (foreignAddress != null && other.foreignAddress != null) {
            return nameMatches && foreignAddress.isSameAddressAs(other.foreignAddress);
        } else {
            return false;
        }
    }

    private boolean trimEquals(final String first, final String second) {
        return trimToEmpty(first).equals(trimToEmpty(second));
    }

}
