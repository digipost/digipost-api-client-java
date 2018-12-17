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

import no.digipost.api.client.representations.AdditionalData;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.Recipients;
import no.digipost.api.client.representations.accounts.UserAccount;
import no.digipost.api.client.representations.accounts.UserInformation;
import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;
import no.digipost.api.client.representations.sender.SenderInformation;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.InputStream;
import java.net.URI;
import java.time.ZonedDateTime;

/**
 * Klasser som implementerer dette interfacet tar seg av de enkelte HTTP-forespørslene
 * man kan gjøre mot REST-API-et:
 *
 * <ul>
 *   <li>Hente søkeforslag (autocomplete)</li>
 *   <li>Søke etter mottakere</li>
 *   <li>Opprette en forsendelsesressurs på serveren</li>
 *   <li>Hente en allerede opprettet forsendelsesressurs fra serveren</li>
 *   <li>Sende innholdet for en allerede opprettet forsendelsesressurs til
 *   serveren, og dermed sende brevet til mottakeren</li>
 *   <li>Opprette en printforsendelsesressurs på serveren</li>
 *   <li>Hente en allerede opprettet printforsendelsesressurs fra serveren</li>
 *   <li>Sende innholdet (PDF) for en allerede opprettet printforsendelsesressurs
 *   til serveren, og dermed bestille print av brevet.</li>
 *   <li>Hente dokument-events, dvs. hendelser knyttet til brev man tidligere har sendt</li>
 * </ul>
 *
 */
public interface ApiService {

    /**
     * Oppretter og sender en multipartforsendelse
     */
    CloseableHttpResponse multipartMessage(HttpEntity multipart);

    /**
     * Oppretter en ny forsendelsesressurs på serveren ved å sende en
     * POST-forespørsel.
     */
    CloseableHttpResponse createMessage(Message message);

    /**
     * Henter en allerede eksisterende forsendelsesressurs fra serveren.
     */
    CloseableHttpResponse fetchExistingMessage(URI location);

    CloseableHttpResponse getEncryptionKey(URI location);

    /**
     * Angir innholdet i en allerede opprettet forsendelse
     *
     * Før man kaller denne metoden, må man allerede ha opprettet en
     * forsendelsesressurs på serveren ved metoden {@code opprettForsendelse}.
     *
     */
    CloseableHttpResponse addContent(Document document, InputStream letterContent);

    /**
     * Sender innholdet i forsendelsen som en POST-forespørsel til serveren
     *
     * OBS! Denne metoden fører til at brevet blir sendt på ordentlig.
     *
     * Før man kaller denne metoden, må man ha lagt innhold til forsendelsen ved
     * metoden {@code addContent}
     *
     */
    CloseableHttpResponse send(MessageDelivery createdMessage);

    /**
     * Legger til ytterligere data til et dokument.
     * Det er en forutsetning at dokumentet har datatype fra tidligere.
     */
    CloseableHttpResponse addData(Document document, AdditionalData data);

    Recipients search(String searchString);

    Autocomplete searchSuggest(String searchString);

    CloseableHttpResponse identifyRecipient(Identification identification);

    /**
     * Sjekker hvis spesifisert mottaker er Digipost-bruker.
     * Returnerer då også publik del av krypteringsnøkkel for Digipost-bruker.
     * Nøkkelen brukes for å kryptere dokument-innhold for dokumenter som
     * skal prekrypteres.
     */
    CloseableHttpResponse identifyAndGetEncryptionKey(Identification identification);

    /**
     * Henter hendelser knyttet til tidligere sendte brev.
     *
     * @param organisation Organisasjonsnummer
     * @param partId Frivillig organisasjons-enhet, kan være {@code null}
     *
     */
    CloseableHttpResponse getDocumentEvents(String organisation, String partId, ZonedDateTime from, ZonedDateTime to, int offset, int maxResults);

    /**
     * Henter status på dokumeter som tidligere blitt sendt i Digipost, både via digital og print-kanal.
     */
    CloseableHttpResponse getDocumentStatus(Link linkToDocumentStatus);
    CloseableHttpResponse getDocumentStatus(long senderId, String uuid);

    CloseableHttpResponse getContent(String path);

    /**
     * Henter publik krypteringsnøkkel for forsendelser som skal sendes til print.
     */
    CloseableHttpResponse getEncryptionKeyForPrint();


    /**
     * Henter informasjon om en faktisk avsender av en melding, altså
     * det mottaker ser som avsender.
     *
     * @param message melding som skal sendes.
     */
    SenderInformation getSenderInformation(MayHaveSender message);


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

    /**
     * Get documents from the inbox for the organisation represented by senderId.
     *
     * @param senderId Either an organisation that you operate on behalf of or your brokerId
     * @param offset Number of documents to skip. For pagination
     * @param limit Maximum number of documents to retrieve (max 1000)
     * @return Inbox element with the n=limit first documents
     */
    Inbox getInbox(SenderId senderId, int offset, int limit);

    /**
     * Get the content of a document as a stream. The content is streamed from the server so remember to
     * close the stream to prevent connection leaks.
     *
     * @param inboxDocument The document to get content for
     * @return Entire content of the document as a stream
     */
    InputStream getInboxDocumentContentStream(InboxDocument inboxDocument);

    /**
     * Delets the given document from the server
     *
     * @param inboxDocument The document to delete
     */
    void deleteInboxDocument(InboxDocument inboxDocument);

    UserAccount createOrActivateUserAccount(SenderId senderId, UserInformation user);
}
