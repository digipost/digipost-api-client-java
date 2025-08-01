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
package no.digipost.api.client.representations;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;

import static no.digipost.api.client.representations.FileType.JPEG;
import static no.digipost.api.client.representations.FileType.PDF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FileTypeTest implements WithQuickTheories {

    @Test
    public void correctEqualsAndHashcode() {
        EqualsVerifier.forClass(FileType.class).verify();
    }

    @Test
    public void normalizesFiletypeStrings() {
        assertThat(new FileType("   PdF  "), is(PDF));
    }

    @Test
    public void resolveFileTypeUsingTheExtensionOfAFileName() {
        qt()
            .forAll(strings().allPossible().ofLengthBetween(0, 30))
            .check(basename -> FileType.fromFilename(basename + ". Jpeg").equals(JPEG));
    }

}
