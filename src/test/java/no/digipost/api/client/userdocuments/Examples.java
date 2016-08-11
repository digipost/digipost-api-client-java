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

import org.apache.http.HttpHost;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Examples {

	public void clientExample() {
		InputStream key = getClass().getResourceAsStream("certificate.p12");

		HttpHost proxy = new HttpHost("proxy.example.com", 8080, "http");

		final BrokerId brokerId = new BrokerId(1234L);
		final SenderId senderId = new SenderId(1234L);

		DigipostUserDocumentClient client = new DigipostUserDocumentClient.Builder(brokerId, key, "password").useProxy(proxy).build();

		final UserId userId = new UserId("01017012345");

		final IdentificationResult identificationResult = client.identifyUser(senderId, userId);
		boolean isDigipost = identificationResult.getResult() == IdentificationResultCode.DIGIPOST;

		client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, true));

		final List<Agreement> agreements = client.getAgreements(senderId, userId);

		final List<Document> documents = client.getDocuments(senderId, AgreementType.INVOICE_BANK, userId);
	}

	public void agreementExamples() throws URISyntaxException {
		InputStream key = getClass().getResourceAsStream("certificate.p12");

		final BrokerId brokerId = new BrokerId(1005);
		final SenderId senderId = new SenderId(1005);

		final DigipostUserDocumentClient.Builder builder = new DigipostUserDocumentClient.Builder(brokerId, key, "password")
				.serviceEndpoint(new URI("https://api.test.digipost.no"))
				.useProxy(new HttpHost("proxy.example.com", 8080))
				.veryDangerouslyDisableCertificateVerificationWhichIsAbsolutelyUnfitForProductionCode();
		final DigipostUserDocumentClient client = builder.build();

		final String requestTrackingId = "testing-testing-testing";
		final UserId userId = new UserId("01017012345");

		//CreateAgreement
		client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, false), requestTrackingId);

		//GetAgreement
		final GetAgreementResult agreement = client.getAgreement(senderId, AgreementType.INVOICE_BANK, userId, requestTrackingId);
		System.out.println(agreement);

		//UpdateAgreement
		client.createOrReplaceAgreement(senderId, Agreement.createInvoiceBankAgreement(userId, true), requestTrackingId);
		final GetAgreementResult modifiedAgreement = client.getAgreement(senderId, AgreementType.INVOICE_BANK, userId, requestTrackingId);
		System.out.println(modifiedAgreement);

		//DeleteAgreement
		client.deleteAgreement(senderId, AgreementType.INVOICE_BANK, userId, requestTrackingId);
	}
}
