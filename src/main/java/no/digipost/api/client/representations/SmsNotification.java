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

import static java.util.Arrays.asList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sms-notification", propOrder = {
	"overrides",
    "ats",
    "afterHours"
})
public class SmsNotification {

	@XmlElement(name = "overrides")
	public final SmsOverrides overrides;
    @XmlElement(name = "at", nillable = false)
    public final List<ListedTime> ats;
    @XmlElement(name = "after-hours", type = Integer.class, nillable = false)
    public final List<Integer> afterHours;

	public SmsNotification() {
		this(0);
	}

	public SmsNotification(final int afterHours) {
		this(afterHours, null);
	}
	public SmsNotification(final int afterHours, final SmsOverrides overrides) {
		this(null, asList(afterHours), overrides);
	}

	public SmsNotification(final List<ListedTime> ats, final List<Integer> afterHours) {
		this(ats, afterHours, null);
	}

	public SmsNotification(final List<ListedTime> ats, final SmsOverrides overrides) {
		this(ats, null, overrides);
	}

	public SmsNotification(final List<ListedTime> ats, final List<Integer> afterHours, final SmsOverrides overrides) {
		this.ats = ats != null ? ats : new ArrayList<ListedTime>();
		this.afterHours = afterHours != null ? afterHours : new ArrayList<Integer>();
		this.overrides = overrides;
	}

}
