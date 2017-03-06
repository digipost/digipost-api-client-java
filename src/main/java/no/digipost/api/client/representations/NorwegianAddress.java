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
@XmlType(name = "norwegian-address", propOrder = { "addressline1", "addressline2", "addressline3", "zipCode", "city" })
public class NorwegianAddress {

    public NorwegianAddress() {
        this(null, null, null);
    }

    public NorwegianAddress(final String zipCode, final String city) {
        this(null, zipCode, city);
    }

    public NorwegianAddress(final String addressline1, final String zipCode, final String city) {
        this(addressline1, null, zipCode, city);
    }

    public NorwegianAddress(final String addressline1, final String addressline2, final String zipCode, final String city) {
        this(addressline1, addressline2, null, zipCode, city);
    }

    public NorwegianAddress(final String addressline1, final String addressline2, final String addressline3, final String zipCode, final String city) {
        this.addressline1 = addressline1;
        this.addressline2 = addressline2;
        this.addressline3 = addressline3;
        this.zipCode = zipCode;
        this.city = city;
    }

    protected String addressline1;
    protected String addressline2;
    protected String addressline3;
    @XmlElement(name = "zip-code", required = true)
    protected String zipCode;
    @XmlElement(required = true)
    protected String city;

    public String getAddressline1() {
        return addressline1;
    }

    public void setAddressline1(final String value) {
        addressline1 = value;
    }

    public String getAddressline2() {
        return addressline2;
    }

    public void setAddressline2(final String value) {
        addressline2 = value;
    }

    public String getAddressline3() {
        return addressline3;
    }

    public void setAddressline3(String value) {
        addressline3 = value;
    }

    public List<String> getAddresslines() {
        List<String> lines = new ArrayList<>(3);
        if (addressline1 != null) lines.add(addressline1);
        if (addressline2 != null) lines.add(addressline2);
        if (addressline3 != null) lines.add(addressline3);
        return lines;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String value) {
        zipCode = value;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String value) {
        city = value;
    }

    public boolean isSameAddressAs(final NorwegianAddress other) {
        return other != null && trimEquals(city, other.city) && trimEquals(zipCode, other.zipCode);
    }

    private boolean trimEquals(final String first, final String second) {
        return trimToEmpty(first).equals(trimToEmpty(second));
    }

}
