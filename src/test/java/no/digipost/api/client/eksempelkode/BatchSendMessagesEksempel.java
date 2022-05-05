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
package no.digipost.api.client.eksempelkode;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.DigipostAddress;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.SmsNotification;
import no.digipost.api.client.representations.batch.Batch;
import no.digipost.api.client.security.Signer;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

public class BatchSendMessagesEksempel {

	// Din virksomhets Digipost-kontoid
	private static final SenderId AVSENDERS_KONTOID = SenderId.of(10987);

	// Passordet sertifikatfilen er beskyttet med
	private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

	public static void main(final String[] args) throws IOException {

		// 1. Vi lager en Signer ved å lese inn sertifikatet du har knyttet til
		// din Digipost-konto (i .p12-formatet)
		Signer signer;
		try (InputStream sertifikatInputStream = lesInnSertifikat()) {
			signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, SERTIFIKAT_PASSORD);
		}

		// 2. Vi oppretter en DigipostClient
		DigipostClient client = new DigipostClient(DigipostClientConfig.newConfiguration().digipostApiUri(URI.create("http://localhost:8282")).build(),
			AVSENDERS_KONTOID.asBrokerId(), signer, HttpClientBuilder.create().setConnectionTimeToLive(2, TimeUnit.MINUTES));

		// 3. Vi må ha en unik id for batchen som skal gå gjennom helle prosessen. Lag deg en og ta var på den!
		final UUID batchUUID = UUID.randomUUID();

		// 4. Vi oppretter en batch i Digipost
		final Batch batch = client.createBatch(batchUUID);


		// 5. Vi oppretter et digipostadresseobjekt
		DigipostAddress address = new DigipostAddress("fornavn.etternavn#6789");

		// 6. Vi oppretter hoveddokumentet
		Document primaryDocument = new Document(UUID.randomUUID(), "Dokumentets emne", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL);

		// 7. Vi oppretter en forsendelse
		Message message = Message.newMessage(UUID.randomUUID(), primaryDocument)
			.batch(batchUUID) // <- VIKTIG: Vi må angi batchUUID hvis ikke blir dokumentet bare sendt direkte. 
			.recipient(address)
			.build();


		// 6. Klientbiblioteket håndterer opprettelse av forsendelse,
		// legge til innhold, og til slutt sending av forsendelsen.
		client.createMessage(message)
			.addContent(primaryDocument, readFileFromDisk("filnavn.pdf"))
			.send();
					

		// 8. Vi må gjøre batchen ferdig og henter den ut sjekker at vi har riktig antall om ønskelig.
		final Batch batchInformation = client.getBatchInformation(batchUUID);
		
		// 9. .. og henter den ut
		final Batch completedBatch = client.completeBatch(batchInformation);
	}

	private static InputStream readFileFromDisk(String filename) {
		// Her må du returnere brevinnholdet du ønsker å sende istedenfor null
		try {
			return new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static InputStream lesInnSertifikat() {
		try {
			// Leser inn sertifikatet
			return new FileInputStream(new File("/path/til/sertifikatfil.p12"));
		} catch (FileNotFoundException e) {
			// Håndter at sertifikatet ikke kunne leses!
			throw new RuntimeException("Kunne ikke lese sertifikatfil: " + e.getMessage(), e);
		}
	}
}
