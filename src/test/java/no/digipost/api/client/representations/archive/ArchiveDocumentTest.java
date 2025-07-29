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

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;

import static co.unruly.matchers.Java8Matchers.where;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;

class ArchiveDocumentTest {

    @Test
    void normal_usage_of_archiveDocument() {
        final ArchiveDocument document = new ArchiveDocument(
                UUID.nameUUIDFromBytes("my known reference for this particular document".getBytes())
                , "minfil.pdf"
                , "pdf"
                , "application/pdf"
        );

        assertThat(document, where(ArchiveDocument::getUuid, equalTo(UUID.fromString("aaf94188-49ba-3e02-ad14-4cdd83b1814b"))));
    }

    @Test
    void you_can_also_use_reference_id_and_random_UUID_and_deletion_time() {
        final ArchiveDocument document = new ArchiveDocument(
                UUID.randomUUID()
                , "minfil.pdf"
                , "pdf"
                , "application/pdf"
        ).withReferenceId("ref:1213").withDeletionTime(ZonedDateTime.now().plusMonths(6));

        assertThat(document, where(ArchiveDocument::getReferenceid, equalTo("ref:1213")));
    }

    @Test
    void add_attributes_to_a_archive_document() {
        final ArchiveDocument document = new ArchiveDocument(
                UUID.randomUUID()
                , "minfil.pdf"
                , "pdf"
                , "application/pdf"
        ).withAttributes(new HashMap<String, String>(){{put("ID", "1234");}});
        assertThat(document, where(ArchiveDocument::getAttributes, contains(
                allOf(
                        where(ArchiveDocumentAttribute::getKey, equalTo("ID")),
                        where(ArchiveDocumentAttribute::getKey, equalTo("ID"))
                )
        )));
    }
}
