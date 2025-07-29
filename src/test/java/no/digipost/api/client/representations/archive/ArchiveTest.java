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
package no.digipost.api.client.representations.archive;

import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.SenderOrganization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static co.unruly.matchers.Java8Matchers.where;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class ArchiveTest {

    @Test
    void should_create_default_archive_for_archiveing_documents() {
        //This is the happycase

        final Archive archive = Archive.defaultArchive()
                .documents(new ArchiveDocument())
                .build();

        assertThat(archive, where(Archive::getDocuments, hasSize(1)));
    }

    @Test
    void senderId_and_SenderOrganizations_can_be_added_but_only_one() {

        Archive.defaultArchive()
                .senderId(SenderId.of(123))
                .documents(new ArchiveDocument())
                .build();

        Archive.defaultArchive()
                .senderOrganization(new SenderOrganization("1337", "R&D"))
                .documents(new ArchiveDocument())
                .build();

        Assertions.assertThrows(IllegalStateException.class, () -> Archive.defaultArchive()
                .senderId(SenderId.of(123))
                .senderOrganization(new SenderOrganization("1337", "R&D"))
                .documents(new ArchiveDocument())
                .build());
    }
}
