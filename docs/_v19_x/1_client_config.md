---
title: Instantiate and configure the client
identifier: client_config
layout: default
---

### Install

The client library is available on the [Maven Central Repository](https://central.sonatype.com/artifact/no.digipost/digipost-api-client-java).
Copy the `<dependency>`-snippet from that website and put it in your pom.xml file.
Make sure to use the latest version available.

This client requires Java 11 and `jakarta.xml-bind`.

### Configure for production use

To instantiate the client instance you need to supply your assigned _broker ID_, which
is set up to be permitted to integrate with the Digipost API. In addition, you must choose
an authentication method. The client supports two:

- **OAuth 2.0 over mutual TLS (JWT/mTLS):** the client obtains access tokens over an
  mTLS-secured channel and sends them as bearer tokens. Use
  `DigipostClient.withJwtMtlsAuthentication(...)`.
- **Certificate-based signing:** each request is signed with a private key. Use
  `DigipostClient.withCertificateAuthentication(...)`.

The chosen method is stated explicitly in the factory method you call.


#### JWT/mTLS authentication

Before you can use the Digipost API using JWT/mTLS, you must register a client with the
Digipost OAuth 2 client authority. Contact the sales team at Digipost to get access to
the client authority and register your client.

Configure a `JwtAuthConfig` with the OAuth 2.0 token endpoint, the resource server URI and
your client ID, together with the client certificate (as a `.p12` keystore) used for the
mutual-TLS handshake against the token endpoint.

```java
SenderId senderId = SenderId.of(123456);

JwtAuthConfig jwtAuthConfig;
try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("client-cert.p12"))) {
    jwtAuthConfig = JwtAuthConfig
            .newConfig(
                    URI.create("https://idp.example.com/oauth2/token"), // token endpoint
                    URI.create("https://api.digipost.no"),              // resource server
                    "your-client-id")
            .pkcs12KeyStore(sertifikatInputStream, "TheSecretPassword")
            .build();
}

DigipostClient client = DigipostClient.withJwtMtlsAuthentication(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), jwtAuthConfig);
```

Access tokens are fetched lazily on first use and cached until shortly before they expire.


#### Certificate-based authentication

Create a `Signer` instance, e.g. by using a `.p12` file to read the private key used to
sign the API requests.

```java
SenderId senderId = SenderId.of(123456);

Signer signer;
try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("certificate.p12"))) {
    signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, "TheSecretPassword");
}

DigipostClient client = DigipostClient.withCertificateAuthentication(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), signer);
```

This example will configure the client to communicate with the regular Digipost production
environment.

### Other environments

If you have access to other environments, this can be configured using
`DigipostClientConfig`, e.g:

```java
URI apiUri = URI.create("https://api.test.digipost.no");
DigipostClientConfig config = DigipostClientConfig.newConfiguration().digipostApiUri(apiUri).build();
```

#### Norsk Helsenett (NHN)

The Digipost API is accessible from both internet and Norsk Helsenett (NHN). Both entry points use
the same API, the only difference is the base URL.

```java
URI nhnApiUri = URI.create("https://api.nhn.digipost.no");
DigipostClientConfig config = DigipostClientConfig.newConfiguration().digipostApiUri(nhnApiUri).build();
```


