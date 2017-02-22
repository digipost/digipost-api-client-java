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

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "foreign-address", propOrder = { "addressline1", "addressline2", "addressline3", "addressline4", "country", "countryCode" })
public class ForeignAddress {

    @XmlElement(required = true)
    protected String addressline1;
    protected String addressline2;
    protected String addressline3;
    protected String addressline4;
    @XmlElement(required = true)
    protected String country;
    @XmlElement(name = "country-code", required = true)
    protected String countryCode;

    public ForeignAddress() {
    }

    public ForeignAddress(String addressline1, String country, String counryCode) {
        this(addressline1, null, null, null, country, counryCode);
    }

    public ForeignAddress(String addressline1, String addressline2, String addressline3, String addressline4, String country, String countryCode) {
        this.addressline1 = addressline1;
        this.addressline2 = addressline2;
        this.addressline3 = addressline3;
        this.addressline4 = addressline4;
        this.country = country;
        this.countryCode = countryCode;
    }

    public String getAddressline1() {
        return addressline1;
    }

    public void setAddressline1(String value) {
        addressline1 = value;
    }

    public String getAddressline2() {
        return addressline2;
    }

    public void setAddressline2(String value) {
        addressline2 = value;
    }

    public String getAddressline3() {
        return addressline3;
    }

    public void setAddressline3(String value) {
        addressline3 = value;
    }

    public String getAddressline4() {
        return addressline4;
    }

    public void setAddressline4(String value) {
        addressline4 = value;
    }

    public List<String> getAddresslines() {
        List<String> lines = new ArrayList<>(3);
        if (addressline1 != null) lines.add(addressline1);
        if (addressline2 != null) lines.add(addressline2);
        if (addressline3 != null) lines.add(addressline3);
        if (addressline4 != null) lines.add(addressline4);
        return lines;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String value) {
        country = value;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isSameAddressAs(ForeignAddress other) {
        return other != null && trimEquals(addressline1, other.addressline1) && trimEquals(country, other.country)
                && trimEquals(countryCode, other.countryCode);
    }

    private boolean trimEquals(final String first, final String second) {
        return trimToEmpty(first).equals(trimToEmpty(second));
    }

}
