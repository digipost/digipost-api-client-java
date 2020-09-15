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
import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.AuthenticationLevel;
import no.digipost.api.client.representations.BankAccountNumber;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.FileType;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.Invoice;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import no.digipost.api.client.representations.MessageRecipient;
import no.digipost.api.client.representations.NameAndAddress;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.PrintDetails;
import no.digipost.api.client.representations.PrintRecipient;
import no.digipost.api.client.representations.SensitivityLevel;
import no.digipost.api.client.representations.SmsNotification;
import no.digipost.api.client.security.Signer;
import no.digipost.api.datatypes.types.Address;
import no.digipost.api.datatypes.types.Appointment;
import no.digipost.api.datatypes.types.Info;
import no.digipost.api.datatypes.types.Language;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class GithubPagesSendExamples {

    private static final SenderId SENDER_ID = SenderId.of(1);
    private static final UUID UUID1 = UUID.randomUUID();
    private static final UUID UUID2 = UUID.randomUUID();
    private static final UUID UUID3 = UUID.randomUUID();
    private static final UUID UUID4 = UUID.randomUUID();
    private static final String CERTIFICATE_PASSWORD = "passord";

    private DigipostClient client;

    public void set_up_client() throws IOException {
        SenderId senderId = SenderId.of(123456);

        Signer signer;
        try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("certificate.p12"))) {
            signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, "TheSecretPassword");
        }

        DigipostClient client = new DigipostClient(
                DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), signer);
    }

    public void send_one_letter_to_recipient_via_personal_identification_number() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();
    }

    public void send_one_letter_to_recipient_via_bank_account_number() throws IOException {
        BankAccountNumber ban = new BankAccountNumber("12345123451");

        Document primaryDocument = new Document(UUID1, "Receipt", FileType.PDF);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(ban)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();
    }

    public void send_one_letter_to_recipient_via_name_and_address() throws IOException {

        NameAndAddress nameAndAddress = new NameAndAddress("Ola Nordmann", "Gateveien 1", "Oppgang B", "0001", "Oslo");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(nameAndAddress)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();

    }

    public void send_one_letter_with_multiple_attachments() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Document attachment1 = new Document(UUID2, "Attachment1 subject", FileType.PDF);

        Document attachment2 = new Document(UUID3, "Attachment2 subject", FileType.PDF);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(pin)
                .attachments(Arrays.asList(attachment1, attachment2))
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("main_document_content.pdf")))
                .addContent(attachment1, Files.newInputStream(Paths.get("attachment1_content.pdf")))
                .addContent(attachment2, Files.newInputStream(Paths.get("attachment2_content.pdf")))
                .send();

    }

    public void send_invoice() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // An invoice requires four extra fields (KID, amount, account and due date). The use of the Invoice class will trigger payment functionality i Digipost.
        Invoice invoice = new Invoice(UUID1, "Invoice subject", FileType.PDF, null, null, null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL, "704279604", new BigDecimal("1.20"), "82760100435", LocalDate.of(2015, 5, 5));

        Message message = Message.newMessage("messageId", invoice)
                .recipient(pin)
                .build();

        client.createMessage(message)
                .addContent(invoice, Files.newInputStream(Paths.get("invoice.pdf")))
                .send();

    }

    public void send_letter_with_sms_notification() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // The time the SMS is sent out can be based on time after letter is delivered or a specific date. This example specifies that the SMS should be sent out one day after the letter i delivered.
        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF, null, new SmsNotification(1), null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();

    }


    public void send_letter_with_fallback_to_print() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        PrintDetails printDetails = new PrintDetails(
                new PrintRecipient("Ola Nordmann", new NorwegianAddress("Prinsensveien 123", "0460", "Oslo")),
                new PrintRecipient("Norgesbedriften", new NorwegianAddress("Akers Àle 2", "0400", "Oslo")), PrintDetails.PrintColors.MONOCHROME, PrintDetails.NondeliverableHandling.RETURN_TO_SENDER);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(new MessageRecipient(pin, printDetails))
                .build();

        // addContent can also take a third parameter which is the file/ipnput stream that will be used only for physical mail. The below example uses the same file/input stream in both channels (digital and physical mail)
        MessageDelivery result = client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();

    }

    public void send_letter_with_higher_security_level() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        // TWO_FACTOR - require BankID or BuyPass authentication to open letter
        // SENSITIVE - Sender information and subject will be hidden until Digipost user is logged in at the appropriate authentication level
        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF, null, null, null, AuthenticationLevel.TWO_FACTOR, SensitivityLevel.SENSITIVE);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();

    }

    public void identify_user_based_on_personal_identification_number() throws IOException {

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Identification identification = new Identification(pin);

        IdentificationResult identificationResult = client.identifyRecipient(identification);

    }

    public void send_letter_through_norsk_helsenett() throws IOException {

        // API URL is different when request is sent from NHN
        DigipostClientConfig config = DigipostClientConfig.newConfiguration().digipostApiUri(URI.create("https://api.nhn.digipost.no")).build();

        Signer signer;
        try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("certificate.p12"))) {
            signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, CERTIFICATE_PASSWORD);
        }

        DigipostClient client = new DigipostClient(config, SENDER_ID.asBrokerId(), signer);

        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();

    }

    public void send_appointment() throws IOException {
        PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

        ZonedDateTime startTime = ZonedDateTime.of(2017, 10, 23, 10, 0, 0, 0, ZoneId.systemDefault());
        Address address = Address.EXAMPLE;
        Info preparation = new Info("Preparation", "Please do not eat or drink 6 hours prior to examination");
        Info about = new Info("About Oslo X-Ray center", "Oslo X-Ray center is specialized in advanced image diagnostics...");
        List<Info> info = Arrays.asList(preparation, about);
        Appointment appointment = new Appointment(
                startTime, startTime.plusMinutes(30), "Please arrive 15 minutes early",
                "Oslo X-Ray center", address, "Lower back examination", info, Language.EN);

        Document primaryDocument = new Document(UUID1, "X-Ray appointment", FileType.PDF, appointment);

        Message message = Message.newMessage("messageId", primaryDocument)
                .recipient(pin)
                .build();

        client.createMessage(message)
                .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
                .send();
    }
}
