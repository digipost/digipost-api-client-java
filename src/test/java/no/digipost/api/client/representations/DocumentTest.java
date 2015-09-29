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

import org.joda.time.DateTime;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import static no.digipost.api.client.representations.FileType.HTML;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class DocumentTest {

	@Test
	public void assertThatDocumentClassHaveNotBeenChangedWithoutChangingDocumentCopyMethod() {
		Field[] messageFields = Document.class.getDeclaredFields();
		assertThat(messageFields.length, is(17));

		String[] allFieldsThatAreUsedForCopyInMessage = new String[]{"UUID_PATTERN", "uuid", "subject", "digipostFileType",
				"opened", "openingReceipt", "smsNotification", "emailNotification", "authenticationLevel", "sensitivityLevel",
				"preEncrypt", "preEncryptNoPages", "contentHash", "technicalType", "isPreEncrypt", "getUuid", "getFileType"};

		for(int i = 0; i < messageFields.length; i++){
			for(int n = 0; n < allFieldsThatAreUsedForCopyInMessage.length; n++){
				if(messageFields[i].getName().equals(allFieldsThatAreUsedForCopyInMessage[n])){
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
		Document originalDoc = new Document(UUID.randomUUID().toString(), "ThisIsASubject", HTML, "OpeningReceipt", new SmsNotification(1),
				new EmailNotification("ny@gmail.com", "Detta", "Er", new ArrayList<ListedTime>()), AuthenticationLevel.IDPORTEN_3,
				SensitivityLevel.NORMAL, false, "technicalType");


		Document copyOfDoc = Document.copyDocumentAndSetDigipostFileTypeToPdf(originalDoc);

		assertThat(originalDoc.digipostFileType, is(HTML.toString()));
		assertThat(copyOfDoc.digipostFileType, is(PDF.toString()));
		assertThat(originalDoc.authenticationLevel, is(copyOfDoc.authenticationLevel));
		assertThat(originalDoc.contentHash, is(copyOfDoc.contentHash));
		assertThat(originalDoc.emailNotification, is(copyOfDoc.emailNotification));
		assertThat(originalDoc.opened, is(copyOfDoc.opened));
		assertThat(originalDoc.openingReceipt, is(copyOfDoc.openingReceipt));
		assertThat(originalDoc.preEncrypt, is(copyOfDoc.preEncrypt));
		assertThat(originalDoc.preEncryptNoPages, is(copyOfDoc.preEncryptNoPages));
		assertThat(originalDoc.sensitivityLevel, is(copyOfDoc.sensitivityLevel));
		assertThat(originalDoc.smsNotification, is(copyOfDoc.smsNotification));
		assertThat(originalDoc.subject, is(copyOfDoc.subject));
		assertThat(originalDoc.uuid, is(copyOfDoc.uuid));
		assertThat(originalDoc.links, is(copyOfDoc.links));
	}
}
