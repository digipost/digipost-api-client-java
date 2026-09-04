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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sms-notification", propOrder = {
    "overrides",
    "alwaysSend",
    "ats",
    "afterHours"
})
public class SmsNotification {

    @XmlElement(name = "overrides")
    public final SmsOverrides overrides;
    @XmlElement(name = "always-send")
    public final boolean alwaysSend;
    @XmlElement(name = "at", nillable = false)
    public final List<ListedTime> ats;
    @XmlElement(name = "after-hours", type = Integer.class, nillable = false)
    public final List<Integer> afterHours;

    public SmsNotification() {
        this(0);
    }

    public SmsNotification(int afterHours) {
        this(null, asList(afterHours));
    }

    public SmsNotification(List<ListedTime> ats, List<Integer> afterHours) {
        this(ats, afterHours, false);
    }

    /**
     * This constructor requires that the sender is allowed to override the
     * recipient's reservation against receiving sender-initiated SMS
     * notifications from Digipost. If {@code alwaysSend} is {@code true}, the
     * notification will also be sent even if the recipient has already read
     * the message before the scheduled notification time, skipping the
     * cancellation that would otherwise happen.
     */
    public SmsNotification(List<ListedTime> ats, List<Integer> afterHours, boolean alwaysSend) {
        this(ats, afterHours, null, alwaysSend);
    }

    /**
     * This constructor requires that the sender is allowed to override SMS
     * preferences with {@link SmsOverrides}.
     */
    public SmsNotification(List<ListedTime> ats, SmsOverrides overrides) {
        this(ats, null, overrides, false);
    }

    /**
     * This constructor requires that the sender is allowed to override SMS
     * preferences with {@link SmsOverrides}, and, if {@code alwaysSend}
     * is {@code true}, that the sender is allowed to override the recipient's
     * reservation against receiving sender-initiated SMS notifications from
     * Digipost. If {@code alwaysSend} is {@code true}, the notification will
     * also be sent even if the recipient has already read the message before
     * the scheduled notification time, skipping the cancellation that would
     * otherwise happen.
     */
    public SmsNotification(List<ListedTime> ats, SmsOverrides overrides, boolean alwaysSend) {
        this(ats, null, overrides, alwaysSend);
    }

    private SmsNotification(List<ListedTime> ats, List<Integer> afterHours, SmsOverrides overrides, boolean alwaysSend) {
        this.ats = ats != null ? new ArrayList<>(ats) : new ArrayList<ListedTime>();
        this.afterHours = afterHours != null ? new ArrayList<>(afterHours) : new ArrayList<Integer>();
        this.overrides = overrides;
        this.alwaysSend = alwaysSend;
    }

}
