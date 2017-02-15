# Inbox API specification

This API enables an organisation (sender) to manage (list, read, delete) documents received in Digipost.

## Table of contents

* [Organisation account](#organisation-account)
* [API technical](#api-technical)
* [Get Inbox](#get-inbox)
* [Get Document content](#get-document-content)
* [Delete document](#delete-document)

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
|deliveryTimeBefore|ISO8601 DateTime|Only get inbox-messages delivered before this timestamp|
|maxResults|int|Maximum number of returned messages (default: 100)|

```http
GET /<sender-id>/inbox?deliveryTimeBefore=2017-02-14T08:25:00+01:00&maxResults=100
Accept: application/vnd.digipost-v7+xml
```

#### Response

```xml
HTTP/1.1 200 Ok

<inbox>
    <inbox-document> <!--zero or more-->
        <id>1234</id>
        <sender>Posten Norge As</sender>
        <delivery-time>2017-05-23T09:30:10+02:00</delivery-time>
        <subject>PUM</subject>
        <authentication-level>TWO FACTOR</authentication-level>
        <first-accessed>2017-02-14T08:25:00+01:00</first-accessed> <!-- or null -->
        <content-type>application/pdf</content-type>
        <content-uri>/1000/inbox/1234/content</content-uri>
        <delete-uri>/1000/inbox/1234</delete-uri>
        <attachment> <!--one or more -->
            <id>2345</id>
            <sender>Posten Norge As</sender>
            <delivery-time>2017-05-23T09:30:10+02:00</delivery-time>
            <subject>Fødselsnummer</subject>
            <authentication-level>TWO FACTOR</authentication-level>
            <first-accessed>2017-02-14T08:25:00+01:00</first-accessed> <!-- or null -->
            <content-type>application/xml</content-type>
            <content-uri>/1000/inbox/2345/content</content-uri>
        </attachment>
    </inbox-document>
</inbox>
```

## Get Document content

#### Java

```java
InputStream content = client.getDocumentContent(inboxDocument);
```

#### Request

```http
GET /<sender-id>/inbox/<document-id>/content
```

#### Response

The document content-uri will return a 307 redirect to a one time, time-limited uri to the actual content. 

```
HTTP/1.1 307 Temporary Redirect
Location: https://www.digipostdata.no/documents/109695014?token=f677fd84c3f3df8fa147cd2cf28bc4a76f521a67b61a28172a0b81e2363d4fe5642e5c0512cb5f75004217427d34cc8599707e61b4eedca3482572d1d2b29b69&download=false
```

## Delete document

#### Java

```java
client.deleteDocument(inboxDocument);
```

#### Request

```http
DELETE /<sender-id>/inbox/<document-id>
```

#### Response

```
HTTP/1.1 200 Ok
```