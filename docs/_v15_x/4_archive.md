---
title: Archive functionality
identifier: archive
layout: default
---

The archive API makes it possible for an organisation to manage documents in archives. These files are kept in separate 
archives, and the files belong to the sender organisation.


## Archive documents to an archive

Let's say you want to archive two documents eg. an invoice and an attachment and
you want to have some kind of reference to both documents. You can do that 
by describing the two documents with `ArchiveDocument`. Then you need to create an archive 
and add the documents to the archive. In the following example we use a default archive.
You then need to send this archive and attach the actual files to the request by linking
the `ArchiveDocument` with a file and send. 

```java
// 1. We describe the documents
final ArchiveDocument invoice = new ArchiveDocument(
    UUID.randomUUID()
    , "invoice_123123.pdf"
    , "pdf"
    , "application/pdf"
);
final ArchiveDocument attachment = new ArchiveDocument(
    UUID.randomUUID()
    , "attachment_123123.pdf"
    , "pdf"
    , "application/pdf"
);

// 2. We create an archive and add the documents to it
Archive archive = Archive.defaultArchive()
    .documents(invoice, attachment)
    .build();

// 3. We create a request to archive the files with reference between the ArchiveDocument and the actual file
client.archiveDocuments(archive)
    .addFile(invoice, readFileFromDisk("invoice_123123.pdf"))
    .addFile(attachment, readFileFromDisk("attachment_123123.pdf"))
    .send();
```

## Get a list of archives

An organisation can have many archives, or just the default unnamed archive. That is up to 
your design wishes. To get a list of the archives for a given Sender, you can do this:

```java
//get a list of the archives
Archives archives = client.getArchives(SenderId.of(123456));
```

The class `Archives` holds a list of `Archive` where you can see the name of the archive.

## Iterate documents in an archive

You _can_ get content of an archive with paged requests. Under is an example of how to iterate
an archive. However, it's use is strongly discouraged because it leads to the idea that 
an archive can be iterated. We expect an archive to possibly reach many million rows so the iteration 
will possibly give huge loads. On the other hand being able to dump all data is a necessary feature of any archive.

_Please use fetch document by UUID or referenceID instead to create functionality on top of the archive._
You should on your side know where and how to get a document from an archive. You do this by knowing where 
you put a file you want to retrieve.

```java
final Archives archives = client.getArchives();

Archive current = archives.getArchives().get(0);
final List<ArchiveDocument> documents = new ArrayList<>();

while (current.getNextDocuments().isPresent()) {
    current = current.getNextDocuments()
    .map(client::getArchiveDocuments)
    .orElse(new Archive());

    documents.addAll(current.getDocuments());
}

// This prints to total content of the list of documents
System.out.println(documents);
```
## Archive Document attributes

You can add optional attributes to documents. An attribute is a key/val string-map that describe documents. You can add 
up to 15 attributes pr. archive document. The attribute key and value is case sensitive.

```java
final ArchiveDocument invoice = new ArchiveDocument(
    UUID.randomUUID()
    , "invoice_123123.pdf"
    , "pdf"
    , "application/pdf"
).withAttribute("INR", "123123").withAttribute("custid", "4321");
```

The attributes can be queried, so that you can get an iterable list of documents.

```java
final Archives archives = digipostClient.getArchives();
Archive current = archives.getArchives().get(0);

final List<ArchiveDocument> documents = current.getNextDocumentsWithAttributes(Map.of("INR", "123123", "custid", "4321"))
        .map(digipostClient::getArchiveDocuments)
        .map(Archive::getDocuments).orElse(emptyList());

// This prints to total content of the list of documents
System.out.println(documents);
```

We recommend that the usage of attributes is made such that the number of results for a query on attributes 
is less than 100. If you still want that, it's ok, but you need to iterate the pages to get all the results.

```java
final Archives archives = client.getArchives();

Archive current = archives.getArchives().get(0);
final List<ArchiveDocument> documents = new ArrayList<>();

while (current.getNextDocuments().isPresent()) {
    current = current.getNextDocumentsWithAttributes(Map.of("INR", "123123"))
    .map(client::getArchiveDocuments)
    .orElse(new Archive());

    documents.addAll(current.getDocuments());
}

// This prints to total content of the list of documents
System.out.println(documents);
```

## Get documents by referenceID

You can retrieve a set of documents by a given referenceID. You will then get the documents listed in their respective
archives in return.

```java
final Archives archives = client.getArchiveDocumentsByReferenceId("REFERENCE_ID");
```

## Get documents by uuid

You can retrieve a set of documents by the UUID that you give the document when you archive it. In the example above
we use `UUID.randomUUID()` to generate an uuid. You can either store that random uuid in your database for
retrieval later, or you can generate a deterministic uuid based on your conventions for later retrieval.

You will get in return an instance of `Archive` which contains information on the archive the document is contained in
and the actual document. From this you can fetch the actual document.

```java
final UUID myConvensionUUID = UUID.fromString("vedlegg:123123:txt");

final Archive archiveWithDocument = client.getArchiveDocumentByUuid(myConvensionUUID);
```

## Get content of a document as a single-use link

You can get the actual content of a document after you have retrieved the archive document. Below is an example of how
you can achieve this with a given `ArchiveDocument`. In the resulting `ArchiveDocumentContent`, you will get a url to
the content which expires after 30 seconds. 

```java
// This ArchiveDocument must be retrieved beforehand using one of the methods described above
final ArchiveDocument archiveDocument;

URI getDocumentContentURI = archiveDocument.getDocumentContent().orElseThrow();
ArchiveDocumentContent content = client.getArchiveDocumentContent(getDocumentContentURI);
```

## Get content of a document as a stream

In addition to a single-use link, you also have the option to retrieve the content of a document directly as a
byte stream. 

```java
// This ArchiveDocument must be retrieved beforehand using one of the methods described above
final ArchiveDocument archiveDocument;

URI getDocumentContentStreamURI = archiveDocument.getDocumentContentStream().orElseThrow();
InputStream content = client.getArchiveDocumentContentStream(getDocumentContentStreamURI);
```

## Update document attributes and/or referenceID

You can add an attribute or change an attribute value, but not delete an attribute. You can however set the value 
to empty string. The value of the field for referenceID can be changed as well. 

```java
final UUID myConvensionUUID = UUID.fromString("vedlegg:123123:txt");

final Archive archiveWithDocument = client.getArchiveDocumentByUuid(myConvensionUUID);

archiveDocument.withReferenceId("My final referenceId").withAttribute("Status", "COMPLETED_PROCESS");

client.updateArchiveDocument(archiveDocument, archiveDocument.getUpdate());
```

## Using archive as a broker

It is possible to be a broker for an actual sender. Most of the api described above also support 
the use of SenderId to specify who you are archiving for.  

eg.:
```java
client.getArchives(SenderId.of(123456))
client.getArchiveDocumentsByReferenceId(SenderId.of(123456), "REFERENCE_ID");
client.getArchiveDocumentByUuid(SenderId.of(123456), myConvensionUUID);


Archive archive = Archive.defaultArchive()
                .documents(faktura)
                .senderId(SenderId.of(123456))
                .build();
```
