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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import static no.digipost.api.client.representations.FileType.HTML;
import static no.digipost.api.client.representations.FileType.PDF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentTest {

    @Test
    public void correctlyReadsTechnicalTypes(){
        String s = Document.parseTechnicalTypes(new String[]{"TekniskType1", "TekniskType2", "", "", "TekniskType3", null, null, "TekniskType4"});
        assertThat(s.length(), is("TekniskType1".length()*4 + 3));
        assertTrue(s.contains("TekniskType1"));
        assertTrue(s.contains("TekniskType2"));
        assertTrue(s.contains("TekniskType3"));
        assertTrue(s.contains("TekniskType4"));

        assertThat(Document.parseTechnicalTypes((String[]) null), nullValue());
        assertThat(Document.parseTechnicalTypes(), nullValue());
        assertThat(Document.parseTechnicalTypes("", "   ", null), nullValue());
    }

    @Test
    public void assertThatDocumentClassHaveNotBeenChangedWithoutChangingDocumentCopyMethod() {
        Field[] messageFields = Document.class.getDeclaredFields();
        assertThat(messageFields, arrayWithSize(13));

        String[] allFieldsThatAreUsedForCopyInMessage = new String[]{"uuid", "subject", "digipostFileType",
                "opened", "openingReceipt", "smsNotification", "emailNotification", "authenticationLevel", "sensitivityLevel",
                "encrypted", "contentHash", "dataType", "technicalType"};

        for (int i = 0; i < messageFields.length; i++) {
            for (int n = 0; n < allFieldsThatAreUsedForCopyInMessage.length; n++) {
                if (messageFields[i].getName().equals(allFieldsThatAreUsedForCopyInMessage[n])) {
                    allFieldsThatAreUsedForCopyInMessage[n] = "";
                }
            }
        }

        for(String shouldBeEmpty : allFieldsThatAreUsedForCopyInMessage){
            assertThat(shouldBeEmpty, is(""));
        }
    }

    @Test
    public void copyOfMessageIsTheSameAsTheOriginalExceptPrintDetails() {
        Document originalDoc = new Document(UUID.randomUUID(), "ThisIsASubject", HTML, "OpeningReceipt", new SmsNotification(1),
                new EmailNotification("ny@gmail.com", "Detta", "Er", new ArrayList<ListedTime>()), AuthenticationLevel.IDPORTEN_3,
                SensitivityLevel.NORMAL, false, null, "technicalType");


        Document copyOfDoc = originalDoc.copyDocumentAndSetDigipostFileTypeToPdf();

        assertThat(originalDoc.digipostFileType, is(HTML.toString()));
        assertThat(copyOfDoc.digipostFileType, is(PDF.toString()));
        assertThat(originalDoc.authenticationLevel, is(copyOfDoc.authenticationLevel));
        assertThat(originalDoc.contentHash, is(copyOfDoc.contentHash));
        assertThat(originalDoc.emailNotification, is(copyOfDoc.emailNotification));
        assertThat(originalDoc.opened, is(copyOfDoc.opened));
        assertThat(originalDoc.openingReceipt, is(copyOfDoc.openingReceipt));
        assertThat(originalDoc.encrypted, is(copyOfDoc.encrypted));
        assertThat(originalDoc.sensitivityLevel, is(copyOfDoc.sensitivityLevel));
        assertThat(originalDoc.smsNotification, is(copyOfDoc.smsNotification));
        assertThat(originalDoc.subject, is(copyOfDoc.subject));
        assertThat(originalDoc.uuid, is(copyOfDoc.uuid));
        assertThat(originalDoc.links, is(copyOfDoc.links));
    }

    @Test
    public void constructorDoesNotThrowNpeWhenNullIsPassedAsOpenedParameter() {
        Document testDoc = new Document(null, null, null, null, null, null, null, null, null, null, "");
        assertThat(testDoc.opened, is(nullValue()));
    }

    @Test
    public void openedIsNullWhenFalseIsPassed() {
        Document primitiveFalseDoc = new Document(null, null, null, null, null, null, null, null, false, null, "");
        assertThat(primitiveFalseDoc.opened, is(nullValue()));
        Document boxedFalseDoc = new Document(null, null, null, null, null, null, null, null, Boolean.valueOf(false), null, "");
        assertThat(boxedFalseDoc.opened, is(nullValue()));
    }

    @Test
    public void isOpenedWhenTrueIsPassed() {
        Document primitiveTrueDoc = new Document(null, null, null, null, null, null, null, null, true, null, "");
        assertThat(primitiveTrueDoc.opened, is(true));
        Document boxedTrueDoc = new Document(null, null, null, null, null, null, null, null, Boolean.valueOf(true), null, "");
        assertThat(boxedTrueDoc.opened, is(true));
    }

    @Test
    public void isOpenedWhenTrueBuiltWithDeprecatedConstructorsIsPassed() {
        Document boolConstructorTrueDoc = new Document(null, null, null, null, null, null, null, null, new Boolean(true), null, "");
        assertThat(boolConstructorTrueDoc.opened, is(true));
        Document stringConstructorTrueDoc = new Document(null, null, null, null, null, null, null, null, new Boolean("true"), null, "");
        assertThat(stringConstructorTrueDoc.opened, is(true));
    }
}
