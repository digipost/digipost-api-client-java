---
title: Use cases
id: usecases
layout: default
description: Use cases for sending digital mail
isHome: false
---

The Java client library also includes <a href="https://github.com/digipost/digipost-api-client-java/tree/master/src/test/java/no/digipost/api/client/eksempelkode">relevant tests</a> that include similar examples to below.

<h3 id="uc01">Send one letter to recipient via personal identification number</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", PDF);

Message message = newMessage(UUID2, primaryDocument)
	.personalIdentificationNumber(pin)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf")
	.send();

{% endhighlight %}

<h3 id="uc02">Send one letter to recipient via name and address</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

NameAndAddress nameAndAddress = new NameAndAddress("Ola Nordmann", "Gateveien 1", "Oppgang B", "0001", "Oslo");

Document primaryDocument = new Document(UUID1, "Document subject", PDF);

Message message = newMessage(UUID2, primaryDocument)
	.nameAndAddress(nameAndAddress)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf")
	.send();

{% endhighlight %}

<h3 id="uc03">Send one letter with multiple attachments</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", PDF);

Document attachment1 = new Document(UUID2, "Attachment1 subject", PDF);

Document attachment2 = new Document(UUID3, "Attachment2 subject", PDF);

Message message = newMessage(UUID4, primaryDocument)
	.personalIdentificationNumber(pin)
	.attachments(asList(attachment1, attachment2))
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("main_document_content.pdf")
	.addContent(attachment, new FileInputStream("attachment_content.pdf"))
	.send();

{% endhighlight %}

<h3 id="uc04">Send invoice</h3>

{% highlight java %}

Coming soon

{% endhighlight %}

<h3 id="uc05">Send letter with SMS notification</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

// The time the SMS is sent out can also be based on time after letter is delivered. new SmsNotification(new DateTime(1) is one day after letter has been delivered
Document primaryDocument = new Document(UUID1, "Document subject", PDF, null, new SmsNotification(new DateTime(2015, 05, 05, 12, 00, 00), null, null, null);

Message message = newMessage(UUID2, primaryDocument)
	.personalIdentificationNumber(pin)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf")
	.send();

{% endhighlight %}

<h3 id="uc07">Send letter with fallback to print</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

Document primaryDocument = new Document(UUID1, "Document subject", PDF);

PrintDetails printDetails = new PrintDetails(
	new PrintRecipient("Ola Nordmann", new NorwegianAddress("Prinsensveien 123", "0460", "Oslo")),
	new PrintRecipient("Norgesbedriften", new NorwegianAddress("Akers Àle 2", "0400", "Oslo")), B, MONOCHROME, RETURN_TO_SENDER);

Message message = newMessage(UUID2, primaryDocument)
	.recipient(new MessageRecipient(pin, printDetails))
	.build();

// addContent can also take a third parameter which is the file/ipnput stream that will be used only for physical mail. The below example uses the same file/input stream in both channels (digital and physical mail)
client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf")
	.send();

{% endhighlight %}

<h3 id="uc08">Send letter with higher security level</h3>

{% highlight java %}

InputStream sertifikatInputStream = new FileInputStream("certificate.p12");

DigipostClient client = new DigipostClient(STEPWISE_REST, "https://api.digipost.no", AVSENDERS_KONTOID, sertifikatInputStream, SERTIFIKAT_PASSORD);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");

// TWO_FACTOR will require BankID or BuyPass to open letter
// SENSITIVE - Sender information and subject will be hidden until Digipost user is logged in at the appropriate authentication level
Document primaryDocument = new Document(UUID1, "Document subject", PDF, null, null, null, TWO_FACTOR, SENSITIVE);

Message message = newMessage(UUID2, primaryDocument)
	.personalIdentificationNumber(pin)
	.build();

client.createMessage(message)
	.addContent(primaryDocument, new FileInputStream("content.pdf")
	.send();

{% endhighlight %}

<h3 id="uc09">Identify user based on personal identification number</h3>

{% highlight java %}

Coming soon

{% endhighlight %}

<h3 id="uc10">Send letter through Norsk Helsenett</h3>

{% highlight java %}

Coming soon

{% endhighlight %}




