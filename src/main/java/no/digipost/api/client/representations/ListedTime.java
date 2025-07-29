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
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Objects;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listedTime")
public final class ListedTime implements Temporal {

    @XmlAttribute(name = "time")
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    public final ZonedDateTime time;

    public ListedTime(){
        this(null);
    }

    public ListedTime(ZonedDateTime atTime) {
        this.time = atTime;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof ListedTime) {
            return Objects.equals(this.time, ((ListedTime) obj).time);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(time);
    }

    @Override
    public boolean isSupported(TemporalField field) {
        return time.isSupported(field);
    }

    @Override
    public long getLong(TemporalField field) {
        return time.getLong(field);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        return time.isSupported(unit);
    }

    @Override
    public ListedTime with(TemporalField field, long newValue) {
        return new ListedTime(time.with(field, newValue));
    }

    @Override
    public ListedTime plus(long amountToAdd, TemporalUnit unit) {
        return new ListedTime(time.plus(amountToAdd, unit));
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        return time.until(endExclusive, unit);
    }

}
