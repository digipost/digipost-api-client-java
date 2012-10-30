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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sms-notification", propOrder = {
    "ats",
    "afterHours"
})
public class SmsNotification {

    @XmlElement(name = "at", nillable = false)
    protected List<ListedTime> ats;
    @XmlElement(name = "after-hours", type = Integer.class, nillable = false)
    protected List<Integer> afterHours;

	public SmsNotification() {

	}

	public SmsNotification(final int afterHours) {
		this.afterHours = new ArrayList<Integer>();
		this.afterHours.add(afterHours);
	}

	/**
     * Gets the value of the ats property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ats property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListedTime }
     * 
     * 
     */
    public List<ListedTime> getAts() {
        if (ats == null) {
            ats = new ArrayList<ListedTime>();
        }
        return this.ats;
    }

    /**
     * Gets the value of the afterHours property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the afterHours property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAfterHours().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getAfterHours() {
        if (afterHours == null) {
            afterHours = new ArrayList<Integer>();
        }
        return this.afterHours;
    }

    public void setAts(List<ListedTime> value) {
        this.ats = null;
        List<ListedTime> draftl = this.getAts();
        draftl.addAll(value);
    }

    public void setAfterHours(List<Integer> value) {
        this.afterHours = null;
        List<Integer> draftl = this.getAfterHours();
        draftl.addAll(value);
    }

}
