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
package no.digipost.api.client.eksempelkode;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.representations.*;
import no.digipost.api.datatypes.types.Appointment;
import no.digipost.api.datatypes.types.AppointmentAddress;
import no.digipost.api.datatypes.types.Info;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static no.digipost.api.client.DigipostClientConfig.DigipostClientConfigBuilder.newBuilder;

public class GithubPagesSendExamples {

    private static final long SENDER_ID = 1;
    private static final String UUID1 = UUID.randomUUID().toString();
    private static final String UUID2 = UUID.randomUUID().toString();
    private static final String UUID3 = UUID.randomUUID().toString();
    private static final String UUID4 = UUID.randomUUID().toString();
    private static final String CERTIFICATE_PASSWORD = "passord";

    private DigipostClient client;

    public void set_up_client() throws FileNotFoundException {
        long senderId = 123456;

        DigipostClient client = new DigipostClient(
                new DigipostClientConfig.DigipostClientConfigBuilder().build(),
                "https://api.digipost.no",
                senderId,
                new FileInputStream("certificate.p12"), "TheSecretPassword");
    }

    public void send_one_letter_to_recipient_via_personal_identification_number() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Message message = Message.MessageBuilder.newMessage("messageId", primaryDocument)
                .personalIdentificationNumber(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();
    }

    public void send_one_letter_to_recipient_via_name_and_address() throws IOException {

        NameAndAddress nameAndAddress = new NameAndAddress("Ola Nordmann", "Gateveien 1", "Oppgang B", "0001", "Oslo");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
                .nameAndAddress(nameAndAddress)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();

    }

    public void send_one_letter_with_multiple_attachments() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Document attachment1 = new Document(UUID2, "Attachment1 subject", FileType.PDF);

        Document attachment2 = new Document(UUID3, "Attachment2 subject", FileType.PDF);

        Message message = Message.MessageBuilder.newMessage(UUID4, primaryDocument)
                .personalIdentificationNumber(pin)
                .attachments(Arrays.asList(attachment1, attachment2))
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("main_document_content.pdf"))
                .addContent(attachment1, new FileInputStream("attachment1_content.pdf"))
                .addContent(attachment2, new FileInputStream("attachment2_content.pdf"))
                .send();

    }

    public void send_invoice() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // An invoice requires four extra fields (KID, amount, account and due date). The use of the Invoice class will trigger payment functionality i Digipost.
        Invoice invoice = new Invoice(UUID1, "Invoice subject", FileType.PDF, null, null, null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL, "704279604", new BigDecimal("1.20"), "82760100435", LocalDate.of(2015, 5, 5));

        Message message = Message.MessageBuilder.newMessage(UUID2, invoice)
                .personalIdentificationNumber(pin)
                .build();

        client.createMessage(message)
                .addContent(invoice, new FileInputStream("invoice.pdf"))
                .send();

    }

    public void send_letter_with_sms_notification() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // The time the SMS is sent out can be based on time after letter is delivered or a specific date. This example specifies that the SMS should be sent out one day after the letter i delivered.
        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF, null, new SmsNotification(1), null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL);

        Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
                .personalIdentificationNumber(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();

    }

    public void send_letter_with_fallback_to_print() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        PrintDetails printDetails = new PrintDetails(
                new PrintRecipient("Ola Nordmann", new NorwegianAddress("Prinsensveien 123", "0460", "Oslo")),
                new PrintRecipient("Norgesbedriften", new NorwegianAddress("Akers Àle 2", "0400", "Oslo")), PrintDetails.PostType.B, PrintDetails.PrintColors.MONOCHROME, PrintDetails.NondeliverableHandling.RETURN_TO_SENDER);

        Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
                .recipient(new MessageRecipient(pin, printDetails))
                .build();

        // addContent can also take a third parameter which is the file/ipnput stream that will be used only for physical mail. The below example uses the same file/input stream in both channels (digital and physical mail)
        MessageDelivery result = client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();

    }

    public void send_letter_with_higher_security_level() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // TWO_FACTOR - require BankID or BuyPass authentication to open letter
        // SENSITIVE - Sender information and subject will be hidden until Digipost user is logged in at the appropriate authentication level
        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF, null, null, null, AuthenticationLevel.TWO_FACTOR, SensitivityLevel.SENSITIVE);

        Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
                .personalIdentificationNumber(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();

    }

    public void identify_user_based_on_personal_identification_number() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Identification identification = new Identification(pin);

        IdentificationResult identificationResult = client.identifyRecipient(identification);

    }

    public void send_letter_through_norsk_helsenett() throws IOException {

        InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

        // API URL is different when request is sent from NHN
        DigipostClient client = new DigipostClient(newBuilder().build(), "https://api.nhn.digipost.no", SENDER_ID, sertifikatInputStream, CERTIFICATE_PASSWORD);

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
                .personalIdentificationNumber(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();

    }

    public void send_appointment() throws FileNotFoundException {
        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        ZonedDateTime startTime = ZonedDateTime.of(2017, 10, 23, 10, 0, 0, 0, ZoneId.systemDefault());
        AppointmentAddress address = new AppointmentAddress("Storgata 1", "0001", "Oslo");
        Info preparation = new Info("Preparation", "Please do not eat or drink 6 hours prior to examination");
        Info about = new Info("About Oslo X-Ray center", "Oslo X-Ray center is specialized in advanced image diagnostics...");
        final List<Info> info = Arrays.asList(preparation, about);
        Appointment appointment = new Appointment(startTime, startTime.plusMinutes(30), "Please arrive 15 minutes early", "Oslo X-Ray center", address, "Lower back examination", info);

        Document primaryDocument = new Document(UUID1, "X-Ray appointment", FileType.PDF, Collections.singletonList(appointment));

        Message message = Message.MessageBuilder.newMessage("messageId", primaryDocument)
                .personalIdentificationNumber(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, new FileInputStream("content.pdf"))
                .send();
    }
}
