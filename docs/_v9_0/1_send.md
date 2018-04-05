---
title: Send messages
identifier: send
layout: default
---

The Java client library also includes [relevant tests](https://github.com/digipost/digipost-api-client-java/tree/master/src/test/java/no/digipost/api/client/eksempelkode) that include similar examples to below.

### Instantiate and configure the client

```java
long senderId = 123456;

DigipostClient client = new DigipostClient(
        new DigipostClientConfig.DigipostClientConfigBuilder().build(),
        "https://api.digipost.no",
        senderId,
        new FileInputStream("certificate.p12"), "TheSecretPassword");
```

### Send one letter to recipient via personal identification number

```java
PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

Message message = Message.MessageBuilder.newMessage("messageId", primaryDocument)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("content.pdf"))
        .send();
```

### Send one letter to recipient via name and address

```java
NameAndAddress nameAndAddress = new NameAndAddress("Ola Nordmann", "Gateveien 1", "Oppgang B", "0001", "Oslo");

Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
        .nameAndAddress(nameAndAddress)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("content.pdf"))
        .send();
```

### Send one letter with multiple attachments

```java
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
```

### Send invoice

```java
PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

// An invoice requires four extra fields (KID, amount, account and due date). The use of the Invoice class will trigger payment functionality i Digipost.
Invoice invoice = new Invoice(UUID1, "Invoice subject", FileType.PDF, null, null, null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL, "704279604", new BigDecimal("1.20"), "82760100435", LocalDate.of(2015, 5, 5));

Message message = Message.MessageBuilder.newMessage(UUID2, invoice)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(invoice, new FileInputStream("invoice.pdf"))
        .send();
```

### Send letter with SMS notification

```java
PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

// The time the SMS is sent out can be based on time after letter is delivered or a specific date. This example specifies that the SMS should be sent out one day after the letter i delivered.
Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF, null, new SmsNotification(1), null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL);

Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("content.pdf"))
        .send();
```

### Send letter with fallback to print

```java
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
```

### Send letter with higher security level

```java
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
```

### Identify user based on personal identification number

```java
InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(newBuilder().build(), ApiFlavor.ATOMIC_REST, "https://api.digipost.no", SENDER_ID, sertifikatInputStream, CERTIFICATE_PASSWORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Identification identification = new Identification(pin);

IdentificationResult identificationResult = client.identifyRecipient(identification);
```

### Send letter through Norsk Helsenett

The Digipost API is accessible from both internet and Norsk Helsenett (NHN). Both entry points use the same API, the only difference is the base URL.

```java
InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

// API URL is different when request is sent from NHN
DigipostClient client = new DigipostClient(newBuilder().build(), ApiFlavor.ATOMIC_REST, "https://api.nhn.digipost.no", SENDER_ID, sertifikatInputStream, CERTIFICATE_PASSWORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("content.pdf"))
        .send();
```

### Send a message with extra computer readable data

Starting with version 9 of the Digipost API, messages can have extra bits of computer readable information that
allows the creation of a customized, dynamic user experience for messages in Digipost. These extra bits of
information are referred to as "Datatypes".

All datatypes are sent in the same way. Each document can accommodate one datatype-object. An exhaustive list of
available datatypes and their documentation can be found at
[digipost/digipost-data-types](https://github.com/digipost/digipost-data-types).

For convenience, all datatypes are available as java-classes in the java client library.

#### Datatype Appointment

In this example, an appointment-datatype that allows for certain calendar-related functions is added to a
message.

```java
PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");
UUID messageUUID = UUID.randomUUID();

ZonedDateTime startTime = ZonedDateTime.of(2017, 10, 23, 10, 0, 0, 0, ZoneId.systemDefault());
AppointmentAddress address = new AppointmentAddress("Storgata 1", "0001", "Oslo");
Info preparation = new Info("Preparation", "Please do not eat or drink 6 hours prior to examination");
Info about = new Info("About Oslo X-Ray center", "Oslo X-Ray center is specialized in advanced image diagnostics...");
List<Info> info = Arrays.asList(preparation, about);

Appointment appointment = new Appointment(startTime, startTime.plusMinutes(30), "Please arrive 15 minutes early", "Oslo X-Ray center", address, "Lower back examination", info);

Document primaryDocument = new Document(messageUUID, "X-Ray appointment", FileType.PDF, appointment);

Message message = Message.MessageBuilder.newMessage("messageId", primaryDocument)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("content.pdf"))
        .send();
```

#### Datatype ExternalLink

This Datatype enhances a message in Digipost with a button which sends the user to an external site. The button
can optionally have a deadline, a description and a custom text. 

```java
PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");
UUID messageUUID = UUID.randomUUID();

URI externalLinkTarget = URI.create("https://example.org/loan-offer/uniqueCustomerId/");
ZonedDateTime deadline = ZonedDateTime.of(2018, 10, 23, 10, 0, 0, 0, ZoneId.systemDefault());

ExternalLink externalLink = new ExternalLink(externalLinkTarget, deadline, "Please read the terms, and use the button above to accept them. The offer expires at 23/10-2018 10:00.", "Accept offer");

Document primaryDocument = new Document(messageUUID, "Housing loan application", FileType.PDF, externalLink);

Message message = Message.MessageBuilder.newMessage("messageId", primaryDocument)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("terms.pdf"))
        .send();
```
