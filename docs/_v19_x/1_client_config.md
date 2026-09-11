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
- **Certificate-based signing (legacy):** each request is signed with a private key. Use
  `DigipostClient.withCertificateAuthentication(...)`.

The chosen method is stated explicitly in the factory method you call.


#### JWT/mTLS authentication

Before you can use the Digipost API using JWT/mTLS, you must register a client with the
[Digipost OAuth 2 client authority (Nyva)](https://nyva.digipost.no). Contact the sales team at Digipost to get access to
the client authority and register your client. More information can be found in the [Digipost API Documentation](https://digipost.github.io/digipost-technical-docs/).

Configure a `JwtAuthConfig` with your client ID and the client certificate (as a `.p12`
keystore) used for the mutual-TLS handshake against the token endpoint. The token endpoint
defaults to the production one, so it only has to be set for other environments.

```java
SenderId senderId = SenderId.of(123456);

JwtAuthConfig jwtAuthConfig;
try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("client-cert.p12"))) {
    jwtAuthConfig = JwtAuthConfig
            .newConfig("your-client-id")
            .pkcs12KeyStore(sertifikatInputStream, "TheSecretPassword")
            .build();
}

DigipostClient client = DigipostClient.withJwtMtlsAuthentication(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), jwtAuthConfig);
```

Access tokens are fetched lazily on first use and cached until shortly before they expire.
They are requested for the API given by `DigipostClientConfig.digipostApiUri`, so you do
not configure the API URI in two places.

Should the API nevertheless answer `401 Unauthorized`, the cached token is discarded and the
request is sent once more with a newly fetched one. Only requests whose content can be sent
again are retried, and only once: if the new token is rejected as well, the error is passed on
to you.

The access tokens are fetched with a separate HTTP client, as it has to present the client
certificate configured above in the TLS handshake against the token endpoint. Its timeouts
(and proxy, connection pool, ...) can be configured with `tokenEndpointHttpSettings(..)`:

```java
JwtAuthConfig jwtAuthConfig = JwtAuthConfig
        .newConfig("your-client-id")
        .pkcs12KeyStore(sertifikatInputStream, "TheSecretPassword")
        .tokenEndpointHttpSettings(
                HttpClientSettings.DEFAULT.timeouts(HttpClientDefaults.DEFAULT_TIMEOUTS_MS.connect(2000).connectionRequest(1000)),
                HttpClientConnectionSettings.DEFAULT.socketTimeout(5000))
        .build();
```

Both parameters have sensible defaults, so pass `HttpClientSettings.DEFAULT` or
`HttpClientConnectionSettings.DEFAULT` for the one you do not need to change. The timeouts of
the client talking to the Digipost API itself are configured separately, with the
`HttpClientBuilder` accepted by `DigipostClient.withJwtMtlsAuthentication(..)`.


#### Certificate-based authentication (legacy)

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

When using JWT/mTLS, also point `JwtAuthConfig` at the token endpoint of that environment:

```java
JwtAuthConfig jwtAuthConfig = JwtAuthConfig
        .newConfig("your-client-id")
        .tokenEndpoint("https://midp.test.digipost.no/oauth2/token")
        .pkcs12KeyStore(sertifikatInputStream, "TheSecretPassword")
        .build();
```

#### Norsk Helsenett (NHN)

The Digipost API is accessible from both internet and Norsk Helsenett (NHN). Both entry points use
the same API, the only difference is the base URL.

```java
URI nhnApiUri = URI.create("https://api.nhn.digipost.no");
DigipostClientConfig config = DigipostClientConfig.newConfiguration().digipostApiUri(nhnApiUri).build();
```


