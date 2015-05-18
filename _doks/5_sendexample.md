---
title: Example code
id: sendexample
layout: default
description: Example code for different use-cases.
isHome: false
---



<h3 id="ex01">Send one document</h3>

{% highlight csharp%}

var config = new ClientConfig(senderId: "xxxxx"); //init config
Logging.Initialize(config); //init logger
var client = new DigipostClient(config, thumbprint: "84e492a972b7edc197a32xxxxxxxxxxxxx".ToUpper()); //init client

//compose Message
var message = new Message(
    new Recipient(IdentificationChoice.PersonalidentificationNumber, "311084xxxx"),
    new Document(subject: "Attachment", fileMimeType: "txt", pathToDocument: @"c:\documents\shipment04\doc_14.txt");
    );

//Send message
var result = client.Send(message);

//Handle response 
Logging.Log(TraceEventType.Information, result.StatusMessage);

{% endhighlight%}

<h3 id="ex02">Send one document and multiple attachments</h3>


{% highlight csharp%}

var config = new ClientConfig("xxxxx"); //init config
Logging.Initialize(config); //init logger
var client = new DigipostClient(clientConfig: config, thumbprint: "84e492a972b7edc197a32xxxxxxxxxxxxx".ToUpper()); //init client

//init documents
var primaryDocument = new Document(subject: "Primary document", fileMimeType: "pdf", pathToDocument: @"c:\documents\shipment04\primaryDocument.pdf");
var attachment1 = new Document(subject: "Attachment 1", fileMimeType: "pdf", pathToDocument: @"c:\documents\shipment04\attachment_01.pdf");
var attachment2 = new Document(subject: "Attachment 2", fileMimeType: "pdf", pathToDocument: @"c:\documents\shipment04\attachment_02.pdf");

//compose Message
var message = new Message(
    new Recipient(identificationChoice: IdentificationChoice.PersonalidentificationNumber, id: "311084xxxx"), primaryDocument
    ) {Attachments = {attachment1,attachment2}};

//Send message
var result = client.Send(message);

//Handle response 
Logging.Log(TraceEventType.Information, result.StatusMessage);


{% endhighlight%}

<h3 id="ex03">Send one document with SMS notification</h3>

{% highlight csharp%}
var config = new ClientConfig("xxxxx"); //init config
Logging.Initialize(config); //init logger
var client = new DigipostClient(clientConfig: config, thumbprint: "84e492a972b7edc197a32xxxxxxxxxxxxx".ToUpper()); //init client

//init documents
var primaryDocument = new Document(subject: "Primary document", fileMimeType: "pdf", pathToDocument: @"c:\documents\primaryDocument.pdf");

primaryDocument.SmsNotification = new SmsNotification(afterHours: 0); //SMS reminder after 0 hour
primaryDocument.SmsNotification.At.Add(new Listedtime(new DateTime(2015, 05, 05, 12, 00, 00))); //new reminder at a specific date


//compose Message
var message = new Message(
    new Recipient(identificationChoice: IdentificationChoice.PersonalidentificationNumber, id: "311084xxxx"), primaryDocument);

//Send message
var result = client.Send(message);

//Handle response 
Logging.Log(TraceEventType.Information, result.StatusMessage);



{% endhighlight%}
