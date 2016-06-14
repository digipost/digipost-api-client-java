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

import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.IdentificationResultCode;
import org.apache.http.HttpHost;

import java.io.InputStream;
import java.util.List;

public class Examples {

	public void clientExample() {
		InputStream key = getClass().getResourceAsStream("certificate.p12");

		HttpHost proxy = new HttpHost("proxy.example.com", 8080, "http");

		DigipostUserDocumentClient client = new DigipostUserDocumentClient.Builder(1234L, key, "password").useProxy(proxy).build();

		final UserId userId = new UserId("01017012345");

		final IdentificationResult identificationResult = client.identifyUser(userId);
		boolean isDigipost = identificationResult.getResult() == IdentificationResultCode.DIGIPOST;

		client.createAgreement(Agreement.createInvoiceBankAgreement("01017012345", true));

		final List<Agreement> agreements = client.getAgreements(userId);
	}
}
