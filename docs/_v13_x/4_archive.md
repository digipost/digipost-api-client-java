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
    , "123123"
    , null
);
final ArchiveDocument attachment = new ArchiveDocument(
    UUID.randomUUID()
    , "attachment_123123.pdf"
    , "pdf"
    , "application/pdf"
    , "123123"
    , ZonedDateTime.now().plusMonths(6)
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
an archive. However it's use is strongly discouraged because it leads to the idea that 
an archive can be itereated. We expect an archive to possibly reach many million rows so the iteration 
will possibly give huge loads. On the other hand being able to dump all data is a nessary feature of any archive.

_Please use fetch document by UUID or referenceID instead to create functionality on top of the archive._
You should on your side know where and how to get a document from an archive. You do this by knowing where 
you put a file you want to retrieve.

```java
final Archives archives = client.getArchives(SenderId.of(123456));

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

## Get documents by referenceID

You can retrieve a set of documents by a given referenceID. You will then get the documents listed in their respective
archives in return.

```java
final Archives archives = client.getArchiveDocumentsByReferenceId(SenderId.of(123456), "REFERENCE_ID");
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

