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

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DateTimeXmlAdapterTest {

    private final DateTimeXmlAdapter adapter = new DateTimeXmlAdapter();

    @Test
    public void marshall_unmarshall_roundtrip_yields_equal_objects() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("GMT-8")).truncatedTo(MILLIS);
        assertThat(adapter.unmarshal(adapter.marshal(now)), is(now));
    }

    @Test
    public void unmarshall_yields_datetime_with_region_based_zoneId_replaced_with_GMT_offset() {
        ZoneId newYorkZone = ZoneId.of("America/New_York");
        ZonedDateTime rightNowInAmerica = ZonedDateTime.now(newYorkZone).truncatedTo(MILLIS);;
        String xmlDateTimeString = adapter.marshal(rightNowInAmerica);

        boolean daylightSavings = newYorkZone.getRules().isDaylightSavings(rightNowInAmerica.toInstant());
        ZoneId gmtZone = daylightSavings ? ZoneId.of("GMT-4") : ZoneId.of("GMT-5");
        assertThat(adapter.unmarshal(xmlDateTimeString), is(rightNowInAmerica.withZoneSameInstant(gmtZone)));
    }
}
