---
title: Receive messages
identifier: inbox
layout: default
---

The inbox API makes it possible for an organisation to manage messages received in Digipost.



## Get documents in inbox

The inbox call outputs a list of documents ordered by delivery time. `Offset` is the start index of the list, and `limit` is the max number of documents to be returned. The `offset` and `limit` is therefore not in any way connected to `InboxDocument.id`.

The values `offset` and `limit` is meant for pagination so that one can fetch 100 and then the next 100.


```java
//get first 100 documents
Inbox first100 = client.getInbox(SenderId.of(123456), 0, 100);

//get next 100 documents
Inbox next100 = client.getInbox(SenderId.of(123456), 100, 100);
```

We have now fetched the 200 newest inbox documents. As long as no new documents are received, the two API-calls shown above will always return the same result. If we now receive a new document, this will change. The first 100 will now contain 1 new document and 99 documents we have seen before. This means that as soon as you stumble upon a document you have seen before you can stop processing, given that all the following older ones have been processed.

## Download document content

```java
Inbox inbox = client.getInbox(SenderId.of(123456));

InboxDocument documentMetadata = inbox.documents.get(0);

System.out.println("Content type is: " + documentMetadata.getContentType());
InputStream documentContent = client.getInboxDocumentContent(documentMetadata);
```

## Delete document

```java
Inbox inbox = client.getInbox(SenderId.of(123456));

InboxDocument documentMetadata = inbox.documents.get(0);

client.deleteInboxDocument(documentMetadata);
```

## Download attachment content

```java
Inbox inbox = client.getInbox(SenderId.of(123456));

InboxDocument documentMetadata = inbox.documents.get(0);
InboxDocument attachment = documentMetadata.getAttachments().get(0);

System.out.println("Content type is: " + attachment.getContentType());
InputStream attachmentContent = client.getInboxDocumentContent(attachment);
```

