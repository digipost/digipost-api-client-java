---
title: Receive messages
identifier: inbox
layout: default
---

<p>The inbox API makes it possible for an organisation to manage messages received in Digipost.</p>

<h3 id="inbox01">Get documents in inbox</h3>

{% highlight java %}

long senderId = 123456;

DigipostClient client = new DigipostClient(
        new DigipostClientConfig.DigipostClientConfigBuilder().build(),
        ApiFlavor.ATOMIC_REST,
        "https://api.digipost.no",
        senderId,
        new FileInputStream("certificate.p12"), "TheSecretPassword");

//get first 100 documents
final Inbox first100 = client.getInbox(new SenderId(senderId), 0, 100);

//get next 100 documents
final Inbox next100 = client.getInbox(new SenderId(senderId), 100, 100);

{% endhighlight %}

<h3 id="inbox01">Download document content</h3>

{% highlight java %}

long senderId = 123456;

DigipostClient client = new DigipostClient(
        new DigipostClientConfig.DigipostClientConfigBuilder().build(),
        ApiFlavor.ATOMIC_REST,
        "https://api.digipost.no",
        senderId,
        new FileInputStream("certificate.p12"), "TheSecretPassword");

final Inbox inbox = client.getInbox(new SenderId(senderId));

final InboxDocument documentMetadata = inbox.documents.get(0);

System.out.println("Content type is: " + documentMetadata.getContentType());
final InputStream documentContent = client.getInboxDocumentContent(documentMetadata);

{% endhighlight %}

<h3 id="inbox01">Delete document</h3>

{% highlight java %}

long senderId = 123456;

DigipostClient client = new DigipostClient(
        new DigipostClientConfig.DigipostClientConfigBuilder().build(),
        ApiFlavor.ATOMIC_REST,
        "https://api.digipost.no",
        senderId,
        new FileInputStream("certificate.p12"), "TheSecretPassword");

final Inbox inbox = client.getInbox(new SenderId(senderId));

final InboxDocument documentMetadata = inbox.documents.get(0);

client.deleteInboxDocument(documentMetadata);

{% endhighlight %}

<h3 id="inbox01">Download attachment content</h3>

{% highlight java %}

long senderId = 123456;

DigipostClient client = new DigipostClient(
        new DigipostClientConfig.DigipostClientConfigBuilder().build(),
        ApiFlavor.ATOMIC_REST,
        "https://api.digipost.no",
        senderId,
        new FileInputStream("certificate.p12"), "TheSecretPassword");

final Inbox inbox = client.getInbox(new SenderId(senderId));

final InboxDocument documentMetadata = inbox.documents.get(0);
final InboxDocument attachment = documentMetadata.getAttachments().get(0);

System.out.println("Content type is: " + attachment.getContentType());
final InputStream attachmentContent = client.getInboxDocumentContent(attachment);

{% endhighlight %}
