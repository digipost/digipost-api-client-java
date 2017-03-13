# Inbox API specification

This API enables an organisation (sender) to manage (list, read, delete) documents received in Digipost.

## Table of contents

* [Organisation account](#organisation-account)
* [API technical](#api-technical)
* [Get Inbox](#get-inbox)
* [Get Document content](#get-document-content)
* [Delete document](#delete-document)
* [Security](#security)

## Organisation account

Before an organisation can access the API it must be registered in Digipost with an organisation account linked to it's organisation number.

### Broker

The broker is the organisation and system integrating with the API. A broker can access the API on behalf of itself or on behalf of other organisations called senders. A broker uses a `brokerId` and a certificate to authenticate with the API.

### Sender

The sender is the organization that wants access to it's inbox in Digipost. Every sender has a `senderId` which is a mandatory parameter to all API-requests. If the organisation has its own integration with the API, the `senderId` will be the same ID as the `brokerId`.

## API technical

* Type: XML over HTTP RESTful API
* Media type for XML: `application/vnd.digipost-v7+xml`
* [Java client library available](https://github.com/digipost/digipost-api-client-java)

## Get Inbox

#### Java

```java
Inbox inbox = client.getInbox(senderId);
```

#### Request

|Parameter|Type  |Description|
|---------|------|-----------|
|offset|int|Skip *offset* number of documents for pagination (default: 0)|
|limit|int|Maximum number of returned documents (default: 100)|

```http
GET /<sender-id>/inbox?offset=0&limit=100
Accept: application/vnd.digipost-v7+xml
```

#### Response

```xml
HTTP/1.1 200 Ok

<inbox>
    <document> <!--zero or more, type: inbox-document -->
        <id>1234</id>
        <subject>PUM</subject>
        <sender>Posten Norge As</sender>
        <delivery-time>2017-05-23T09:30:10+02:00</delivery-time>
        <first-accessed>2017-02-14T08:25:00+01:00</first-accessed> <!-- or null -->
        <authentication-level>TWO FACTOR</authentication-level>
        <content-type>application/pdf</content-type>
        <content-uri>/1000/inbox/1234/content</content-uri>
        <delete-uri>/1000/inbox/1234</delete-uri>
        <attachment> <!--zero or more, type: inbox-document -->
            <id>2345</id>
            <subject>Fødselsnummer</subject>
            <sender>Posten Norge As</sender>
            <delivery-time>2017-05-23T09:30:10+02:00</delivery-time>
            <first-accessed>2017-02-14T08:25:00+01:00</first-accessed> <!-- or null -->
            <authentication-level>TWO FACTOR</authentication-level>
            <content-type>application/xml</content-type>
            <content-uri>/1000/inbox/2345/content</content-uri>
        </attachment>
    </document>
</inbox>
```

## Get document content

#### Java

```java
InputStream content = client.getDocumentContent(senderId, document);
```

#### Request

```http
GET /<sender-id>/inbox/<document-id>/content
```

#### Response

The document content-uri will return a 307 redirect to a one time, time-limited uri to the actual content. 

```http
HTTP/1.1 307 Temporary Redirect
Location: https://www.digipostdata.no/documents/109695014?token=f677fd84c3f3df8fa147cd2cf28bc4a76f521a67b61a28172a0b81e2363d4fe5642e5c0512cb5f75004217427d34cc8599707e61b4eedca3482572d1d2b29b69&download=false
```

## Delete document

#### Java

```java
client.deleteDocument(senderId, document);
```

#### Request

```http
DELETE /<sender-id>/inbox/<document-id>
```

#### Response

```http
HTTP/1.1 200 Ok
```

## Security

The API employs both transport layer security (SSL/TLS) and message level digital signature to protect confidentiality, integrity and nonrepudiation.

### Transport security (SSL/TLS)

API endpoints are only available through HTTPS using secure versions of TLS.

### Message signatures

Every request issued by the client and every response from the server must contain a digital signature over selected header values including request method and path, client ID, timestamp and a SHA256 hash of the entire message body.

#### X509 Certificate

Third party systems must register a X509 certificate with Digipost. This certificate will be used to verify the signature in each request.

#### Headers

The following example shows the mandatory security related headers:

```
Date: Wed, 29 Jun 2016 14:58:11 GMT
X-Digipost-UserId: 9999
X-Content-SHA256: q1MKE+RZFJgrefm34/uplM/R8/si9xzqGvvwK0YMbR0=
X-Digipost-Signature: BHvtgDTKz490iMbYZsOf5+FvWCsWDt5oJgyTvXlLiNrWgUu/fhuY8AJYBoH8g+0t46slsmJqQxNlsa6u+cF1aE921cZy7ISSeRLl/z6WlwCtTGu9fFH9X4Kr+2ffwPqzCTRPD4D5jHrbudmSGZJIq3ImAKU250t6SCJ//aiAKMg=
```

#### Signature

The signature is computed using the `SHA256WithRSAEncryption` algorithm over canonical string as shown below:

POST

```
POST
/messages
date: Wed, 29 Jun 2016 14:58:11 GMT
x-content-sha256: q1MKE+RZFJgrefm34/uplM/R8/si9xzqGvvwK0YMbR0=
x-digipost-userid: 9999
parameter1=58&parameter2=test
```

GET

```
GET
/
date: Wed, 29 Jun 2016 14:58:11 GMT
x-digipost-userid: 9999
parameter1=58&parameter2=test
```

Pseudo code for generating the signature

```java
String stringToSign = uppercase(verb) + "\n" +
                      lowercase(path) + "\n" +
                      "date: " + datoHeader + "\n" +
                      "x-content-sha256: " + sha256Header + "\n" +
                      "x-digipost-userid: " + virksomhetsId + "\n" +
                      lowercase(urlencode(requestparametre)) + "\n";

String signature =    base64(sign(stringToSign));
```

#### Reference

https://digipost.no/plattform/api/v5/sikkerhet

#### Document content token

When a client requests the content uri of a Document a one-time, time limited uniqe uri is generated. The URI looks like:

`https://www.digipostdata.no/documents/34303129?token=30a6648a2cb1ce05d31dd6188135d7107c87d353dfe60f7720a598c4d6a95c2e4cf05f3ab63e52d734d745c2bf5084d37347f58aeca9da743235cf37cdca0ecb&download=false`

The token part is generated with the following algorithm:

```
tokenBasis = documentId + SECRET_STRING + UUID.randomUUID()
tokenHashBytes = SHA512(tokenBasis)
token = String(HexEncode(tokenHashBytes))
```

The token is stored together with the documentId, created timestamp and some other metadata related to the document. The token is valid for `30` seconds.

When the user requests the generated URI the token is validated against the stored version. The documentId from the URI must also match the documentId associated with the token. The URI can only be used once.
