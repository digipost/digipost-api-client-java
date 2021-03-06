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

