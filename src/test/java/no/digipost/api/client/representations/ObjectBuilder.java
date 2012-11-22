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

	public static PrintRecipient newNorwegianRecipient(final String name, final String zip, final String city) {
		return newNorwegianRecipient(name, null, null, zip, city);
	}

	public static PrintRecipient newNorwegianRecipient(final String name, final String address1, final String address2, final String zip,
			final String city) {
		NorwegianAddress norwegianAddress = new NorwegianAddress(zip, city);
		PrintRecipient printRecipient = new PrintRecipient(name, norwegianAddress);
		norwegianAddress.setAddressline1(address1);
		norwegianAddress.setAddressline2(address2);
		return printRecipient;
	}

	public static PrintRecipient newForeignAddress(final String name, final String addressline1, final String country, final String countryCode) {
		ForeignAddress foreignAddress = new ForeignAddress(addressline1, country, countryCode);
		PrintRecipient printRecipient = new PrintRecipient(name, foreignAddress);
		return printRecipient;
	}

	public static Message newPrintMessage(final String string, final PrintRecipient recipient, final PrintRecipient returnAddress) {
		return new Message(string, new PrintDetails(recipient, returnAddress, "B"));
	}

}
