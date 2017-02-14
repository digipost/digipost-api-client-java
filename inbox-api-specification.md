# Inbox API specification

This API enables an organisation (sender) to manage (list, get, delete) messages received in Digipost.

## Table of contents

...

## Organisation account

Before an organisation can access the API it must be registered in Digipost with an organisation account linked to it's organisation number.

### Broker

The broker is the organisation and system integrating with the API. A broker can access the API on behalf of itself or on behalf of other organisations called senders. A broker uses a `brokerId` and a certificate to authenticate with the API.

### Sender

The sender is the organization that wants access to it's inbox in Digipost. Every sender has a `senderId` which is a mandatory parameter to all API-requests. If the organisation has its own integration with the API, the `senderId` will be the same ID as the `brokerId`.

## API technical

* Type: XML over HTTP RESTful API
* Media type for XML: `application/vnd.digipost-v7+xml`
* [Java client library available](#java-client-library)

## Get Inbox

#### Java

```java
Inbox inbox = client.getInbox(senderId);
```

#### Request

|Parameter|Type  |Description|
|---------|------|-----------|
|startIndex|int|Paging start at index (default: 0)|
|maxResult|int|Maximum number of returned messages (default: 100)|

```http
GET /<senderId>/inbox?startIndex=0&maxResult=100
Accept: application/vnd.digipost-v7+xml
```

#### Response

```xml
HTTP/1.1 200 Ok

<inbox>
    <inbox-message> <!--zero or more-->
        <sender>Posten Norge As</sender>
        <deliveryTime>2017-05-23T09:30:10+02:00</deliveryTime>
        <inbox-document> <!--one or more -->
            <subject>PUM</subject>
            <authentication-level>TWO FACTOR</authentication-level>
            <firstAccessed>2017-02-14T08:25:00+01:00</firstAccessed> <!-- or null -->
            <document-content-uri>/1000/inbox/1234</document-content-uri>
            <document-content-type>application/pdf</document-content-type>
        </inbox-document>
    </inbox-message>
</inbox>
```

## Get Document content

#### Java

```java
InputStream content = client.getDocumentContent(inboxDocument);
```

#### Request

```http
GET /<senderId>/inbox/<documentId>
Accept: */*
```

#### Response

```
HTTP/1.1 200 Ok

pdf...content...data...
```

## Delete document

#### Java

```java
client.deleteDocument(inboxDocument);
```

#### Request

```http
DELETE /<senderId>/inbox/<documentId>
```

#### Response

```
HTTP/1.1 200 Ok
```