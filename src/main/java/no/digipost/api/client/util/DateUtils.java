package no.digipost.api.client.util;
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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.tz.FixedDateTimeZone;

public class DateUtils {

	public static final String RFC_1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
	private static final DateTimeFormatter fmt = DateTimeFormat.forPattern(RFC_1123_DATE_FORMAT).withZone(FixedDateTimeZone.forID("GMT"));

	/**
	 * Returns an RFC 1123 date format used in HTTP
	 */
	public static String formatDate(DateTime dateTime) {
		return fmt.print(dateTime);
	}

	/**
	 * Return an DateTime parsed from an RFC 1123 compliant string
	 */
	public static DateTime parseDate(String dateTime) {
		return fmt.parseDateTime(dateTime);
	}

}
