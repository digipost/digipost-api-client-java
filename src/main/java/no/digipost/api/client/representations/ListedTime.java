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

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;
import org.joda.time.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Objects;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listedTime")
public final class ListedTime implements ReadableInstant {

    @XmlAttribute(name = "time")
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    public final DateTime time;

	public ListedTime(){
		this(null);
	}

	public ListedTime(DateTime atTime) {
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
    public int compareTo(ReadableInstant o) {
		return time.compareTo(o);
    }

	@Override
    public long getMillis() {
		return time.getMillis();
    }

	@Override
    public Chronology getChronology() {
		return time.getChronology();
    }

	@Override
    public DateTimeZone getZone() {
		return time.getZone();
    }

	@Override
    public int get(DateTimeFieldType type) {
		return time.get(type);
    }

	@Override
    public boolean isSupported(DateTimeFieldType field) {
		return time.isSupported(field);
    }

	@Override
    public Instant toInstant() {
		return time.toInstant();
    }

	@Override
    public boolean isEqual(ReadableInstant instant) {
		return time.isEqual(instant);
    }

	@Override
    public boolean isAfter(ReadableInstant instant) {
		return time.isAfter(instant);
    }

	@Override
    public boolean isBefore(ReadableInstant instant) {
		return time.isBefore(instant);
    }

}
