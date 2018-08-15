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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.Document.technicalAttachment;
import static no.digipost.api.client.representations.FileType.GIF;
import static no.digipost.api.client.representations.FileType.HTML;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.FileType.ZIP;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class MessageTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldBeDirectPrintWhenMessageContainsOnlyPrintDetails() {
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF))
                .recipient(new MessageRecipient(new PrintDetails()))
                .build();
        assertTrue(message.isDirectPrint());
    }

    @Test
    public void assertThatClassesHaveNotBeenChangedWithoutChangingMessageCopyMethod() {
        Field[] messageFields = Message.class.getDeclaredFields();
        assertThat(messageFields.length, is(8));

        String[] allFieldsThatAreUsedForCopyInMessage = new String[]{"messageId", "senderId", "senderOrganization",
        "recipient", "deliveryTime", "invoiceReference", "primaryDocument", "attachments"};

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

        Field[] recipientFields = MessageRecipient.class.getDeclaredFields();
        assertThat(recipientFields.length, is(6));

        String[] allFieldsThatAreUsedForCopyInMessageRecipient = new String[]{"nameAndAddress", "digipostAddress", "personalIdentificationNumber",
                "organisationNumber", "printDetails"};

        for(int i = 0; i < recipientFields.length; i++){
            for(int n = 0; n < allFieldsThatAreUsedForCopyInMessageRecipient.length; n++){
                if(recipientFields[i].getName().equals(allFieldsThatAreUsedForCopyInMessageRecipient[n])){
                    allFieldsThatAreUsedForCopyInMessageRecipient[n] = "";
                }
            }
        }
    }

    @Test
    public void copyOfMessageIsTheSameAsTheOriginalExceptPrintDetails() {
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", HTML))
                .digipostAddress(new DigipostAddress("Test2"))
                .senderId(1L).deliveryTime(ZonedDateTime.now()).invoiceReference("Invoice")
                .recipient(new MessageRecipient(new DigipostAddress("TestAdress"), new PrintDetails(
                        new PrintRecipient("Test", new NorwegianAddress("Bajs", "Korv", "Zip", "Zop")),
                        new PrintRecipient("Test", new NorwegianAddress("Bajs", "Korv", "Zip", "Zop")),
                        PrintDetails.PrintColors.COLORS, PrintDetails.NondeliverableHandling.RETURN_TO_SENDER))).build();

        Message copyOfMessageWithPrintDetailsOnly = Message.copyMessageWithOnlyPrintDetails(message);

        assertThat(copyOfMessageWithPrintDetailsOnly.deliveryTime, is(message.deliveryTime));
        assertThat(copyOfMessageWithPrintDetailsOnly.invoiceReference, is(message.invoiceReference));
        assertThat(copyOfMessageWithPrintDetailsOnly.messageId, is(message.messageId));
        assertThat(copyOfMessageWithPrintDetailsOnly.senderId, is(message.senderId));
        assertNull(copyOfMessageWithPrintDetailsOnly.recipient.digipostAddress);
        assertNull(copyOfMessageWithPrintDetailsOnly.recipient.nameAndAddress);
        assertNull(copyOfMessageWithPrintDetailsOnly.recipient.organisationNumber);
        assertNull(copyOfMessageWithPrintDetailsOnly.recipient.personalIdentificationNumber);
        assertThat(copyOfMessageWithPrintDetailsOnly.recipient.printDetails.nondeliverableHandling, is(message.recipient.printDetails.nondeliverableHandling));
        assertThat(copyOfMessageWithPrintDetailsOnly.recipient.printDetails.printColors, is(message.recipient.printDetails.printColors));

        assertThat("When copying to only print, the file type should be set to pdf",
                copyOfMessageWithPrintDetailsOnly.primaryDocument.digipostFileType, is(PDF.toString()));
        assertThat("When copying to only print, the file type should be set to pdf", copyOfMessageWithPrintDetailsOnly.getAllDocuments().map(d -> d.digipostFileType).collect(toList()), everyItem(is(PDF.toString())));

        assertThat(copyOfMessageWithPrintDetailsOnly.recipient.printDetails.recipient.name, is(message.recipient.printDetails.recipient.name));
        assertThat(copyOfMessageWithPrintDetailsOnly.recipient.printDetails.recipient.norwegianAddress.addressline1, is(message.recipient.printDetails.recipient.norwegianAddress.addressline1));

        Message copyOfMessageWithDigipostDetailsOnly = Message.copyMessageWithOnlyDigipostDetails(message);

        assertThat(copyOfMessageWithDigipostDetailsOnly.deliveryTime, is(message.deliveryTime));
        assertThat(copyOfMessageWithDigipostDetailsOnly.invoiceReference, is(message.invoiceReference));
        assertThat(copyOfMessageWithDigipostDetailsOnly.messageId, is(message.messageId));
        assertThat(copyOfMessageWithDigipostDetailsOnly.senderId, is(message.senderId));
        assertThat(copyOfMessageWithDigipostDetailsOnly.recipient.digipostAddress, is(message.recipient.digipostAddress));
        assertThat(copyOfMessageWithDigipostDetailsOnly.recipient.organisationNumber, is(message.recipient.organisationNumber));
        assertThat(copyOfMessageWithDigipostDetailsOnly.recipient.personalIdentificationNumber, is(message.recipient.personalIdentificationNumber));
        assertNull(copyOfMessageWithDigipostDetailsOnly.recipient.printDetails);

        assertThat(copyOfMessageWithDigipostDetailsOnly.primaryDocument.digipostFileType, is(HTML.toString()));
        assertThat(copyOfMessageWithDigipostDetailsOnly.getAllDocuments().map(doc -> doc.digipostFileType).collect(toList()), everyItem(is(HTML.toString())));
    }

    @Test
    public void shouldNotBeDirectPrintWhenMessageContainsDigipostAddress() {
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .digipostAddress(new DigipostAddress("test.testson#1234"))
                .build();
        assertFalse(message.isDirectPrint());
    }
    @Test
    public void shouldNotBeDirectPrintWhenMessageContainsNameAndAddress() {
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .recipient(new MessageRecipient(new NameAndAddress()))
                .build();
        assertFalse(message.isDirectPrint());
    }
    @Test
    public void shouldNotBeDirectPrintWhenMessageContainsPersonalIdendificationNumber() {
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL))
                .personalIdentificationNumber(new PersonalIdentificationNumber("12125412435"))
                .build();
        assertFalse(message.isDirectPrint());
    }

    @Test
    public void possibleToPassNullForNoAttachments() {
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF))
                .digipostAddress(new DigipostAddress("test.testson#1234"))
                .build();
        assertThat(message.attachments, hasSize(0));
    }

    @Test
    public void changingThePassedAttachmentListDoesNotChangeTheMessage() {
        List<Document> attachments = new ArrayList<Document>(asList(new Document(UUID.randomUUID().toString(), "subject", PDF), new Document(UUID.randomUUID().toString(), "subject", PDF)));
        Message message = newMessage(UUID.randomUUID().toString(), new Document(UUID.randomUUID().toString(), "subject", PDF))
                .digipostAddress(new DigipostAddress("test.testson#1234"))
                .attachments(attachments)
                .build();
        attachments.clear();

        assertThat(attachments, empty());
        assertThat(message.attachments, hasSize(2));
    }

    @Test
    public void sortsDocumentsByTheSameOrderAsTheyAppearInTheMessage() {
        Document hoved = new Document(UUID.randomUUID().toString(), "hoved", GIF);
        Document a1 = new Document(UUID.randomUUID().toString(), "a1", PDF);
        Document a2 = technicalAttachment(ZIP, "uhu, så teknisk!");
        Document a3 = new Document(UUID.randomUUID().toString(), "a3", HTML);
        Message message = newMessage("id", hoved).attachments(asList(a1, a2, a3)).digipostAddress(new DigipostAddress("blah#ABCD")).build();

        assertThat(Stream.of(a2, hoved, a3, a1).sorted(message.documentOrder()).collect(toList()), contains(hoved, a1, a2, a3));
    }

    @Test
    public void sortingDocumentsNotInMessageByOrderInMessageThrowsException() {
        Document hoved = new Document(UUID.randomUUID().toString(), "hoved", GIF);
        Document a1 = new Document(UUID.randomUUID().toString(), "a1", PDF);
        Document a2 = technicalAttachment(ZIP, "uhu, så teknisk!");
        Document notInMessage = new Document(UUID.randomUUID().toString(), "a3", HTML);
        Message message = newMessage("id", hoved).attachments(asList(a1, a2)).digipostAddress(new DigipostAddress("blah#ABCD")).build();

        expectedException.expect(Message.CannotSortDocumentsUsingMessageOrder.class);
        expectedException.expectMessage("ikke sortere Document med uuid '" + notInMessage.uuid);
        Collections.sort(asList(a2, hoved, notInMessage, a1), message.documentOrder());
    }
}
