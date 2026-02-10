/*
 * Copyright (C) Posten Bring AS
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
package no.digipost.api.client.delivery;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.AddDataLink;
import no.digipost.api.client.representations.AdditionalData;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.Recipients;
import no.digipost.api.client.representations.accounts.UserAccount;
import no.digipost.api.client.representations.accounts.UserInformation;
import no.digipost.api.client.representations.sender.SenderInformation;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;

import java.net.URI;

/**
 * Klasser som implementerer dette interfacet tar seg av de enkelte meldingsrelaterte
 * HTTP-forespørslene man kan gjøre mot Digipost sitt API:
 *
 * <ul>
 *   <li>Sende meldinger til mottakere i Digipost og/eller print</li>
 *   <li>Hente søkeforslag (autocomplete)</li>
 *   <li>Søke etter mottakere</li>
 *   <li>Sende innholdet for en allerede opprettet forsendelsesressurs til
 *   serveren, og dermed sende brevet til mottakeren</li>
 *   <li>Hente dokument-events, dvs. hendelser knyttet til brev man tidligere har sendt</li>
 * </ul>
 *
 */
public interface MessageDeliveryApi {

    /**
     * Oppretter og sender en multipartforsendelse
     */
    ClassicHttpResponse sendMultipartMessage(HttpEntity multipart);

    /**
     * Legger til ytterligere data til et dokument.
     * Det er en forutsetning at dokumentet har datatype fra tidligere.
     */
    ClassicHttpResponse addData(AddDataLink document, AdditionalData data);

    Recipients search(String searchString);

    Autocomplete searchSuggest(String searchString);

    ClassicHttpResponse identifyRecipient(Identification identification);

    /**
     * Sjekker hvis spesifisert mottaker er Digipost-bruker.
     * Returnerer då også publik del av krypteringsnøkkel for Digipost-bruker.
     * Nøkkelen brukes for å kryptere dokument-innhold for dokumenter som
     * skal prekrypteres.
     */
    ClassicHttpResponse identifyAndGetEncryptionKey(Identification identification);


    ClassicHttpResponse getEncryptionKey(URI location);

    /**
     * Henter public krypteringsnøkkel i x509 format for forsendelser som skal sendes til print.
     */
    ClassicHttpResponse getEncryptionCertificateForPrint();

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
    SenderInformation getSenderInformation(SenderId senderId);

    /**
     * Henter informasjon om en avsender. Avsender må ha godtatt å identifiseres med
     * organisasjonsnummer og ev. underenhet.
     *
     * @param orgnr organisasjonsnummeret til avsenderen.
     * @param avsenderenhet underenhet for et organisasjonsnummer.
     */
    SenderInformation getSenderInformation(String orgnr, String avsenderenhet);


    UserAccount createOrActivateUserAccount(SenderId senderId, UserInformation user);
}
