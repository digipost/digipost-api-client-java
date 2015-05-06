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
package no.digipost.api.client;

import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderInformation;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.joda.time.DateTime;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.net.URI;

/**
 * Klasser som implementerer dette interfacet tar seg av de enkelte HTTP-forespørslene
 * man kan gjøre mot REST-API-et, nemlig:
 *
 * <ul>
 * <li>Hente søkeforslag (autocomplete)</li>
 * <li>Søke etter mottakere</li>
 * <li>Opprette en forsendelsesressurs på serveren
 * <li>Hente en allerede opprettet forsendelsesressurs fra serveren
 * <li>Sende innholdet for en allerede opprettet forsendelsesressurs til
 * serveren, og dermed sende brevet til mottakeren
 * <li>Opprette en printforsendelsesressurs på serveren
 * <li>Hente en allerede opprettet printforsendelsesressurs fra serveren
 * <li>Sende innholdet (PDF) for en allerede opprettet printforsendelsesressurs
 * til serveren, og dermed bestille print av brevet.
 * <li>Hente dokument-events, dvs. hendelser knyttet til brev man tidligere har sendt</li>
 *
 * <ul>
 *
 * For å sende et brev gjennom Digipost er det tilstrekkelig å gjøre disse to
 * kallene:
 *
 * <pre>
 * createMessage(message);
 * addToContentAndSend(createdMessage, content);
 * </pre>
 *
 * Dette kan også gjøres ved å kalle metoden {@code sendMessage} i klassen
 * {@code MessageSender}, som i tillegg gjør en del feilhåndtering.
 */
public interface ApiService {
	EntryPoint getEntryPoint();

	/**
	 * Oppretter og sender en multipartforsendelse
	 */
	Response multipartMessage(MultiPart multiPart);

	/**
	 * Oppretter en ny forsendelsesressurs på serveren ved å sende en
	 * POST-forespørsel.
	 */
	Response createMessage(Message message);

	/**
	 * Henter en allerede eksisterende forsendelsesressurs fra serveren.
	 */
	Response fetchExistingMessage(URI location);

	Response getEncryptionKey(URI location);

	/**
	 * Angir innholdet i en allerede opprettet forsendelse
	 *
	 * Før man kaller denne metoden, må man allerede ha opprettet en
	 * forsendelsesressurs på serveren ved metoden {@code opprettForsendelse}.
	 *
	 */
	Response addContent(Document document, InputStream letterContent);

	/**
	 * Sender innholdet i forsendelsen som en POST-forespørsel til serveren
	 *
	 * OBS! Denne metoden fører til at brevet blir sendt på ordentlig.
	 *
	 * Før man kaller denne metoden, må man ha lagt innhold til forsendelsen ved
	 * metoden {@code addContent}
	 *
	 */
	Response send(MessageDelivery createdMessage);

	Recipients search(String searchString);

	Autocomplete searchSuggest(String searchString);

	void addFilter(ClientRequestFilter filter);

	Response identifyRecipient(Identification identification);

	/**
	 * Sjekker hvis spesifisert mottaker er Digipost-bruker.
	 * Returnerer då også publik del av krypteringsnøkkel for Digipost-bruker.
	 * Nøkkelen brukes for å kryptere dokument-innhold for dokumenter som
	 * skal prekrypteres.
	 * @param identification
	 */
	Response identifyAndGetEncryptionKey(Identification identification);

	/**
	 * Henter hendelser knyttet til tidligere sendte brev.
	 *
	 * @param organisation Organisasjonsnummer
	 * @param partId Frivillig organisasjons-enhet, kan være {@code null}
	 *
	 */
	Response getDocumentEvents(String organisation, String partId, DateTime from, DateTime to, int offset, int maxResults);

	/**
	 * Henter status på dokumeter som tidligere blitt sendt i Digipost, både via digital og print-kanal.
	 * @param linkToDocumentStatus
	 */
	Response getDocumentStatus(Link linkToDocumentStatus);
	Response getDocumentStatus(long senderId, String uuid);

	Response getContent(String path);

	/**
	 * Henter publik krypteringsnøkkel for forsendelser som skal sendes til print.
	 */
	Response getEncryptionKeyForPrint();


	/**
	 * Henter informasjon om en avsender.
	 *
	 * @param senderId id-en til avsenderen.
	 */
	SenderInformation getSenderInformation(long senderId);

	/**
	 * Henter informasjon om en avsender. Avsender må ha godtatt å identifiseres med
	 * organisasjonsnummer og ev. underenhet.
	 *
	 * @param orgnr organisasjonsnummeret til avsenderen.
	 * @param avsenderenhet underenhet for et organisasjonsnummer.
	 */
	SenderInformation getSenderInformation(String orgnr, String avsenderenhet);
}
