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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.representations.AuthenticationLevel;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.NameAndAddress;
import no.digipost.api.client.representations.SensitivityLevel;
import no.digipost.api.client.representations.SmsNotification;

import org.apache.commons.io.FileUtils;

/**
 * Kode som brukes i dokumentasjonen for klientbiblioteket.
 * 
 */
public class ForsendelseEksempelNavnogAdresse {
	// Din virksomhets Digipost-kontoid
	private static final long AVSENDERS_KONTOID = 10987;

	// Passordet sertifikatfilen er beskyttet med
	private static final String SERTIFIKAT_PASSORD = "SertifikatPassord123";

	public static void main(final String[] args) {

		// 1. Vi leser inn sertifikatet du har knyttet til din Digipost-konto (i
		// .p12-formatet)
		InputStream sertifikatInputStream = lesInnSertifikat();

		// 2. Vi oppretter en DigipostClient
		DigipostClient client = new DigipostClient("https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

		// 3. Vi oppretter et nameandaddress-objekt
		NameAndAddress nameAndAddress = new NameAndAddress("Ola Nordmann", "Gateveien 1", "Oppgang B", "0001", "Oslo");

		// 4. Vi oppretter en forsendelse
		Message message = new Message("dinForsendelseId", "Brevets emne", nameAndAddress, new SmsNotification(),
				AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL);

		// 5. Vi henter inputstreamen til PDF-filen vi ønsker å sende
		InputStream messageContent = getMessageContent();

		// 6. Vi lar klientbiblioteket håndtere utsendelsen
		client.sendMessage(message, messageContent);
	}

	private static InputStream getMessageContent() {
		// Her må du returnere brevinnholdet du ønsker å sende istedenfor null
		return null;
	}

	private static InputStream lesInnSertifikat() {
		try {
			// Leser inn sertifikatet med Apache Commons FileUtils.
			return FileUtils.openInputStream(new File("/path/til/sertifikatfil.p12"));
		} catch (IOException e) {
			// Håndter at sertifikatet ikke kunne leses!
			throw new RuntimeException("Kunne ikke lese sertifikatfil");
		}
	}
}
