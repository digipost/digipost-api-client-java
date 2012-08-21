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

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.representations.ContentType;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.PrintMessage;
import no.digipost.api.client.representations.PrintRecipient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Kode som brukes i dokumentasjonen for klientbiblioteket.
 * 
 */
public class FallbackTilPrintEksempel {
	// Din virksomhets Digipost-kontoid
	private static final long AVSENDERS_KONTOID = 10987;

	// Passordet sertifikatfilen er beskyttet med
	private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

	public static void main(final String[] args) {

		// 1. For å kunne kryptere brevet som skal sendes trenger vi
		// BouncyCastle
		Security.addProvider(new BouncyCastleProvider());

		// 2. Vi leser inn sertifikatet du har knyttet til din Digipost-konto (i
		// .p12-formatet)
		InputStream sertifikatInputStream = lesInnSertifikat();

		// 3. Vi oppretter en DigipostClient
		DigipostClient client = new DigipostClient("https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

		// 4. Vi oppretter et fødselsnummerobjekt som skal brukes til å
		// identifisere mottaker i Digipost
		PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

		// 5. Vi oppretter en forsendelse for sending av brevet i Digipost
		Message message = new Message("dinForsendelseId", "Brevets emne", pin, false, PASSWORD, NORMAL);

		// 6. Vi oppretter en printforsendelse med adresseinformasjon som vil
		// benyttes dersom mottaker ikke er Digipostbruker
		PrintMessage printMessage = new PrintMessage("en id til", new PrintRecipient("Mottakers navn", new NorwegianAddress("postnummer",
				"Mottakers poststed")), new PrintRecipient("Avsenders navn", new NorwegianAddress("postnummer", "Avsenders poststed")), "A");

		// 7. Foreløpig støtter Digipost kun å sende krypterte brev til print. Å
		// spesifisere PreEncrypt gjør at klientbiblioteket krypterer filen for
		// deg før den oversendes Digipost.
		printMessage.setPreEncrypt(true);

		// 8. Vi henter inputstreamen til PDF-filen vi ønsker å sende
		InputStream messageContent = getMessageContent();

		// 9. Vi lar klientbiblioteket håndtere utsendelsen
		client.sendMessageToDigipostOrDeliverToPrint(message, ContentType.PDF, messageContent, printMessage, messageContent);

		IOUtils.closeQuietly(messageContent);
	}

	private static InputStream getMessageContent() {
		// Her må du returnere brevinnholdet du ønsker å sende istedenfor null
		return null;
	}

	private static InputStream lesInnSertifikat() {
		try {
			// Leser inn sertifikatet selv med Apache Commons FileUtils.
			return FileUtils.openInputStream(new File("/path/til/sertifikatfil.p12"));
		} catch (IOException e) {
			// Håndter at sertifikatet ikke kunne leses!
			throw new RuntimeException("Kunne ikke lese sertifikatfil");
		}
	}
}
