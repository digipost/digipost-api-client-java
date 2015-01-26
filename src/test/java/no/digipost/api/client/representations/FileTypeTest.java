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
package no.digipost.api.client.representations;

import com.pholser.junit.quickcheck.ForAll;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static no.digipost.api.client.representations.FileType.JPEG;
import static no.digipost.api.client.representations.FileType.PDF;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class FileTypeTest {

	@Test
	public void correctEqualsAndHashcode() {
		EqualsVerifier.forClass(FileType.class).verify();
	}

	@Test
	public void normalizesFiletypeStrings() {
		assertThat(new FileType("   PdF  "), is(PDF));
	}

	@Theory
	public void createFileTypeUsingTheExtensionOfAFileName(@ForAll String basenames) {
		assertThat(FileType.fromFilename(basenames + ". Jpeg"), is(JPEG));
	}

}
