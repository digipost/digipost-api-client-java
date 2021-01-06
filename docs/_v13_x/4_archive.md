---
title: Archive functionality
identifier: arhive
layout: default
---

The archive API makes it possible for an organisation to manage documents in archives. These files are kept in separate 
archives, and the files belongs to the sender organisation.


## Archive documents to an archive

Let's say you want to arhive two documents eg. an invoice and an attachment and
you want to have som kind of reference to both documents. You can do that 
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
);
final ArchiveDocument attachment = new ArchiveDocument(
    UUID.randomUUID()
    , "attachment_123123.pdf"
    , "pdf"
    , "application/pdf"
    , "123123"
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

