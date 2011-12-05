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
package no.digipost.api.client.representations.xml;

import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;

@XmlTransient
public class DateTimeXmlAdapter extends XmlAdapter<Date, DateTime> {

	@Override
	public Date marshal(final DateTime v) throws Exception {
		return v == null ? null : new Date(v.getMillis());
	}

	@Override
	public DateTime unmarshal(final Date v) throws Exception {
		return v == null ? null : new DateTime(v.getTime());
	}

}
