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
package no.digipost.api.client.tag;

import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.accounts.Tag;
import no.digipost.api.client.representations.accounts.Tags;

/**
 * Klasser som implementerer dette interfacet håndterer det å sette og fjerne
 * såkalte tags fra brukerkontoer via Digipost sitt API. Tags indikerer egenskaper
 * ved en konto, som f.eks om de har aktivert mottak av offentlig post eller ikke.
 * For å administrere slike tags trengs det nødvendige tilganger til Digipost sitt API

 */
public interface TagApi {

    void addTag(Tag tag);

    void removeTag(Tag tag);

    Tags getTags(PersonalIdentificationNumber personalIdentificationNumber);

}
