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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.01.17 at 04:00:33 PM CET
//


package no.digipost.api.client.representations.sender;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import java.util.Objects;

import static java.util.Objects.hash;
import static java.util.Optional.ofNullable;


/**
 * Java class for feature complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="feature"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://api.digipost.no/schema/v7&gt;feature-name"&gt;
 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "feature", propOrder = {
        "value"
})
public final class SenderFeature {

    @XmlValue
    private final String value;
    @XmlAttribute(name = "param")
    private final String param;


    public SenderFeature() {
        this(null, null);
    }

    public SenderFeature(SenderFeatureName name, String param) {
        this.value = name != null ? name.identificator : null;
        this.param = param;
    }

    public SenderFeatureName getName() {
        return SenderFeatureName.from(value);
    }

    public String getParam() {
        return param;
    }

    public int getIntParam() {
        try {
            return Integer.parseInt(getParam());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("The sender feature " + value + " has the parameter '" + param + "', " +
                                            "which can not be converted to an int. (" + e.getMessage() + ")", e);
        }
    }

    public long getLongParam() {
        try {
            return Long.parseLong(getParam());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("The sender feature " + value + " has the parameter '" + param + "', " +
                                            "which can not be converted to an long. (" + e.getMessage() + ")", e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SenderFeature) {
            SenderFeature that = (SenderFeature) other;
            return Objects.equals(this.value, that.value) &&
                   Objects.equals(this.param, that.param);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash(value, param);
    }

    @Override
    public String toString() {
        return value + ofNullable(param).map(p -> ": " + p).orElse("");
    }
}
