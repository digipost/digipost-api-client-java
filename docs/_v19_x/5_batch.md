---
title: Batch functionality
identifier: batch
layout: default
---

The batch API makes it possible for an organisation to manage sending of several messages, both to Digipost and Print, in a 
batch. The batch will then be delivered all at the same time atomically. If it has not been sendt yet, the batch can 
also be cancelled.

## Start and get information about a batch

A batch is identified by a UUID specified by you. To create a batch you send a uuid to the Digipost api. In return~~~~ 
you get a batch object with a status and som links for complete and cancel. 

```java
// Create an UUID
final UUID batchUUID = UUID.randomUUID();

// Create the batch
final Batch batch = client.createBatch(batchUUID);

// At any time, read information about the batch
final Batch batchInformation = client.getBatchInformation(batchUUID);

```

A batch can have 4 states:
`CREATED`, `NOT_COMMITTED`, `COMMITTED`, `DONE`

CREATED is an initial state. NOT_COMMITTED is the state given when there has been added messages to the batch.
COMMITTED is a state that can occur if the batch has to be processed asynchronously. DONE means that the batch has
been commited. Digipost messages are delivered at commit time and Print messages will be delivered on first
possible work day after commit time.


## Send messages with batch reference.

You can send both Digipost and Print messages just as you normally would, but to attach them to a batch you add the 
batch as a reference on the message. The IMPORTANT part is visible below. Without this, the message will be delivered as
otherwise specified.

```java
// Create an UUID
UUID batchUUID = UUID.randomUUID();

// Create the batch
client.createBatch(batchUUID);

PersonalIdentificationNumber pin = new PersonalIdentificationNumber("26079833787");
UUID documentUuid = UUID.randomUUID();
Document primaryDocument = new Document(documentUuid, "Document subject", FileType.PDF);

Message message = Message.newMessage("messageId", primaryDocument)
        .recipient(pin)
        .build();

client.createMessage(message)
        .batch(batchUUID) // <- IMPORTANT
        .addContent(primaryDocument, Files.newInputStream(Paths.get("content.pdf")))
        .send();
```

## Commit a batch

After you have created a batch and sendt the messages with the batch you need to commit the batch. Without the commit 
Digipost will never send the messages and might at a later time delete the incomplete batch and messages 
referred to in the batch.

To complete the batch, simply complete it:

``` java
// [...]
// get the information and verify that the count of digipost/print messages are as expected
final Batch batchInformation = client.getBatchInformation(batchUUID);

// complete the batch
final Batch completedBatch = client.completeBatch(batchInformation);
```

## Cancel a batch

You can at any time before completion cancel a batch. Cancelling means that the batch will be removed and cannot 
be processed futher. Digipost will immediately delete all documents, messages and other references to the batch.
Any further attempts to fetch information about the batch will throw a 404.

``` java
// [...]
// get the batch information
final Batch batchInformation = client.getBatchInformation(batchUUID);

// cancel the batch
client.cancelBatch(batchInformation);
```
