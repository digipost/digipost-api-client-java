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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.ZonedDateTime;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "print-if-unread", propOrder = {
        "printIfUnreadAfter",
        "printDetails"
})
public class PrintIfUnread {

    @XmlElement(name = "print-if-unread-after", type = String.class)
    @XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected ZonedDateTime printIfUnreadAfter;
    @XmlElement(name = "print-details", required = true)
    protected PrintDetails printDetails;

    PrintIfUnread() {}

    public PrintIfUnread(ZonedDateTime printIfUnreadAfter, PrintDetails printDetails) {
        this.printIfUnreadAfter = printIfUnreadAfter;
        this.printDetails = printDetails;
    }
}
