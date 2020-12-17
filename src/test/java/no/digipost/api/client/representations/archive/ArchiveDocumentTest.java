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
package no.digipost.api.client.representations.archive;

import co.unruly.matchers.Java8Matchers;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
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

        assertThat(document, Java8Matchers.where(ArchiveDocument::getUuid, equalTo(UUID.fromString("aaf94188-49ba-3e02-ad14-4cdd83b1814b"))));
    }

    @Test
    void you_can_also_use_reference_id_and_random_UUID() {
        final ArchiveDocument document = new ArchiveDocument(
                UUID.randomUUID()
                , "minfil.pdf"
                , "pdf"
                , "application/pdf"
                , "ref:1213"
        );

        assertThat(document, Java8Matchers.where(ArchiveDocument::getReferenceid, equalTo("ref:1213")));
    }
}
