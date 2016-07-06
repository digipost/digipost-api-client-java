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
package no.digipost.api.client.userdocuments;

/**
 * User id represented by personal identification number
 */
public class UserId extends JustAValid<String> {

	public static final String QUERY_PARAM_NAME = "user-id";

	public UserId(final String personalIdentificationNumber) {
		super(personalIdentificationNumber, "UserID ", "Personal Identification Number must be an 11-digit string");
	}

	public boolean isValid(String personalIdentificationNumber) {
		return personalIdentificationNumber != null && personalIdentificationNumber.length() == 11;
	}

	public String serialize() {
		return this.value;
	}

	@Deprecated
	public String getFnr() {
		return getPersonalIdentificationNumber();
	}

	public String getPersonalIdentificationNumber() {
		return this.value;
	}

}
