---
title: Use cases
identifier: usecases
layout: default
---

<p>The Java client library also includes <a href="https://github.com/digipost/digipost-api-client-java/tree/master/src/test/java/no/digipost/api/client/eksempelkode">relevant tests</a> that include similar examples to below.</p>

<p>The below examples instantiate a client in each example. This is done for example purposes only.</p>

<h3 id="uc01">Send one letter to recipient via personal identification number</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

Message message = Message.MessageBuilder.newMessage("messageId", primaryDocument)
        .personalIdentificationNumber(pin)
        .build();

client.createMessage(message)
        .addContent(primaryDocument, new FileInputStream("content.pdf"))
        .send();

{% endhighlight %}

<h3 id="uc02">Send one letter to recipient via name and address</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

NameAndAddress nameAndAddress = new NameAndAddress("Ola Nordmann", "Gateveien 1", "Oppgang B", "0001", "Oslo");

Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
	.nameAndAddress(nameAndAddress)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf"))
	.send();

{% endhighlight %}

<h3 id="uc03">Send one letter with multiple attachments</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

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

{% endhighlight %}

<h3 id="uc04">Send invoice</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

// An invoice requires four extra fields (KID, amount, account and due date). The use of the Invoice class will trigger payment functionality i Digipost.
Invoice invoice = new Invoice(UUID1, "Invoice subject", FileType.PDF, null, null, null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL, "704279604", new BigDecimal("1.20"), "82760100435", new LocalDate(2015, 5, 5));

Message message = Message.MessageBuilder.newMessage(UUID2, invoice)
	.personalIdentificationNumber(pin)
	.build();

client.createMessage(message)
	.addContent(invoice, new FileInputStream("invoice.pdf"))
	.send();

{% endhighlight %}

<h3 id="uc05">Send letter with SMS notification</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

// The time the SMS is sent out can be based on time after letter is delivered or a specific date. This example specifies that the SMS should be sent out one day after the letter i delivered.
Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF, null, new SmsNotification(1), null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL);

Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
	.personalIdentificationNumber(pin)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf"))
	.send();

{% endhighlight %}

<h3 id="uc07">Send letter with fallback to print</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

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

{% endhighlight %}

<h3 id="uc08">Send letter with higher security level</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

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

{% endhighlight %}

<h3 id="uc09">Identify user based on personal identification number</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Identification identification = new Identification(pin);

IdentificationResult identificationResult = client.identifyRecipient(identification);

{% endhighlight %}

<h3 id="uc10">Send letter through Norsk Helsenett</h3>

The Digipost API is accessible from both internet and Norsk Helsenett (NHN). Both entry points use the same API, the only difference is the base URL.

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

// API URL is different when request is sent from NHN
DigipostClient client = new DigipostClient(ApiFlavor.STEPWISE_REST, "https://api.nhn.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", FileType.PDF);

Message message = Message.MessageBuilder.newMessage(UUID2, primaryDocument)
	.personalIdentificationNumber(pin)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf"))
	.send();

{% endhighlight %}
