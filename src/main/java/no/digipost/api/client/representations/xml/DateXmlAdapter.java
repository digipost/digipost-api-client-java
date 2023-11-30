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
package no.digipost.api.client.representations.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static jakarta.xml.bind.DatatypeConverter.parseDate;
import static jakarta.xml.bind.DatatypeConverter.printDate;

public class DateXmlAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public String marshal(final LocalDate date) {
        if (date == null) {
            return null;
        }
        GregorianCalendar convertedDate = GregorianCalendar.from(ZonedDateTime.of(date, LocalTime.MIDNIGHT, ZoneId.systemDefault()));
        return printDate(convertedDate);
    }

    @Override
    public LocalDate unmarshal(final String value) {
        if (value == null) {
            return null;
        }
        Calendar parsed = parseDate(value);
        return ZonedDateTime.ofInstant(parsed.toInstant(), parsed.getTimeZone().toZoneId()).toLocalDate();
    }

}
