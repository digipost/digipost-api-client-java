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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identification", propOrder = {
		"nameAndAddress",
		"digipostAddress",
		"personalIdentificationNumber"
})
@XmlRootElement(name = "identification")
public class Identification {

	@XmlElement(name = "name-and-address", nillable = false)
	protected NameAndAddress nameAndAddress;
	@XmlElement(name = "digipost-address", nillable = false)
	protected String digipostAddress;
	@XmlElement(name = "personal-identification-number", nillable = false)
	protected String personalIdentificationNumber;

	public Identification(final NameAndAddress nameAndAddress) {
		this.nameAndAddress = nameAndAddress;
	}

	public Identification(final DigipostAddress digipostAddress) {
		this.digipostAddress = digipostAddress.asString();
	}

	public Identification(final PersonalIdentificationNumber personalIdentificationNumber) {
		this.personalIdentificationNumber = personalIdentificationNumber.asString();
	}
}

