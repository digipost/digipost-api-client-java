---
title: Receive messages
identifier: inbox
layout: default
---

The inbox API makes it possible for an organisation to manage messages received in Digipost.

### Instantiate and configure the client

```java
long senderId = 123456;

DigipostClient client = new DigipostClient(
        new DigipostClientConfig.DigipostClientConfigBuilder().build(),
        "https://api.digipost.no",
        senderId,
        new FileInputStream("certificate.p12"), "TheSecretPassword");
```

### Get documents in inbox

```java
 //get first 100 documents
final Inbox first100 = client.getInbox(new SenderId(123456), 0, 100);

//get next 100 documents
final Inbox next100 = client.getInbox(new SenderId(123456), 100, 100);
```

### Download document content

```java
final Inbox inbox = client.getInbox(new SenderId(123456));

final InboxDocument documentMetadata = inbox.documents.get(0);

System.out.println("Content type is: " + documentMetadata.getContentType());
final InputStream documentContent = client.getInboxDocumentContent(documentMetadata);
```

### Delete document

```java
final Inbox inbox = client.getInbox(new SenderId(123456));

final InboxDocument documentMetadata = inbox.documents.get(0);

client.deleteInboxDocument(documentMetadata);
```

### Download attachment content

```java
final Inbox inbox = client.getInbox(new SenderId(123456));

final InboxDocument documentMetadata = inbox.documents.get(0);
final InboxDocument attachment = documentMetadata.getAttachments().get(0);

System.out.println("Content type is: " + attachment.getContentType());
final InputStream attachmentContent = client.getInboxDocumentContent(attachment);
```
