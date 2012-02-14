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


public class ObjectBuilder {

	public static PrintRecipient newRecipient(final String name, final String zip, final String city, final String country) {
		return newRecipient(name, null, null, zip, city, country);
	}

	public static PrintRecipient newRecipient(final String name, final String address1, final String address2, final String zip,
			final String city, final String country) {
		PrintRecipient printRecipient = new PrintRecipient(name, zip, city);
		printRecipient.setAddressline1(address1);
		printRecipient.setAddressline2(address2);
		printRecipient.setCountry(country);
		return printRecipient;
	}

	public static PrintMessage newPrintMessage(final String string, final PrintRecipient recipient, final PrintRecipient returnAddress) {
		return new PrintMessage(string, recipient, returnAddress, "A");
	}

}
