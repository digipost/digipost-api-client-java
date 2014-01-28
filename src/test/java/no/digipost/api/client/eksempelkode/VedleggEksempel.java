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

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.representations.*;
import org.apache.commons.io.FileUtils;

public class VedleggEksempel {
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

		// 3. Vi oppretter et fødselsnummerobjekt
		PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

		// 4. Vi oppretter hoveddokumentet
		Document primaryDocument = new Document(UUID.randomUUID().toString(), "Hoveddokumentets emne", PDF, null, new SmsNotification(), PASSWORD, NORMAL);

		// 5. Vi oppretter vedlegget
		Document attachment = new Document(UUID.randomUUID().toString(), "Vedleggets emne", PDF, null, new SmsNotification(), PASSWORD, NORMAL);

		// 6. Vi oppretter en forsendelse
		Message message = new Message(UUID.randomUUID().toString(), pin, primaryDocument, asList(attachment));

		// 7. Vi lar klientbiblioteket opprette forsendelsen
		MessageDelivery createdMessage = client.createMessage(message);

		// 8. Vi henter inputstreamen til filen vi vil ha som hoveddokument
		InputStream primaryDocumentContent = getPrimaryDocumentContent();

		// 9. Vi lar klientbiblioteke håndtere å legge til innhold i hoveddokumentet
		client.addContent(createdMessage, primaryDocument, primaryDocumentContent);

		// 10. Vi henter inputstreamen til filen vi vil ha som vedlegg
		InputStream attachmentContent = getAttachmentContent();

		// 11. Vi lar klientbiblioteket håndtere å legge til innhold i vedlegget
		client.addContent(createdMessage, attachment, attachmentContent);

		// 12. Vi lar klientbiblioteket håndtere sending av forsendelsen
		client.sendMessage(createdMessage);
	}

	private static InputStream getPrimaryDocumentContent() {
		// Her må du returnere brevinnholdet du ønsker å sende i stedet for null
		return null;
	}

	private static InputStream getAttachmentContent() {
		// Her må du returnere vedleggsinnholdet du ønsker å sende i stedet for null
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
