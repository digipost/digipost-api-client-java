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
is set up to be permitted to integrate with the Digipost API. The client authenticates
with OAuth 2.0 over mutual TLS (JWT/mTLS): it obtains access tokens over an mTLS-secured
channel and sends them as bearer tokens.

Certificate-based request signing was removed in 19.0.0. If you are upgrading from 18.x,
see [Migrating from certificate-based authentication](#migrating-from-certificate-based-authentication).


#### JWT/mTLS authentication

Before you can use the Digipost API using JWT/mTLS, you must register a client with the
Digipost OAuth 2 client authority. Contact the sales team at Digipost to get access to
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

DigipostClient client = DigipostClient.create(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), jwtAuthConfig);
```

This example will configure the client to communicate with the regular Digipost production
environment.

Access tokens are fetched lazily on first use and cached until shortly before they expire.
They are requested for the API given by `DigipostClientConfig.digipostApiUri`, so you do
not configure the API URI in two places.

Should the API nevertheless answer `401 Unauthorized`, the client discards the cached token,
fetches a new one and sends the request again. This is done at most once per request: if the
API rejects the new token as well, the `401` is handled like any other error response.

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
`HttpClientBuilder` accepted by `DigipostClient.create(..)`.


### Custom HTTP client

The client uses Apache HttpClient 5 internally. If you need control over the underlying
HTTP client (e.g. timeouts or proxy settings), you can supply your own `HttpClientBuilder`:

```java
HttpClientBuilder clientBuilder = HttpClientFactory.createDefaultBuilder();

DigipostClient client = DigipostClient.create(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), jwtAuthConfig, clientBuilder);
```

Note that the connection manager of the builder you pass is replaced: the client needs one
configured with the client certificate, so that the mTLS handshake against the API succeeds.


### Other environments

If you have access to other environments, this can be configured using
`DigipostClientConfig`, e.g:

```java
URI apiUri = URI.create("https://api.test.digipost.no");
DigipostClientConfig config = DigipostClientConfig.newConfiguration().digipostApiUri(apiUri).build();
```

Also point `JwtAuthConfig` at the token endpoint of that environment:

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


### Migrating from certificate-based authentication

Version 19.0.0 removes certificate-based request signing. `Signer`,
`Signer.usingKeyFromPKCS12KeyStore(..)` and
`DigipostClient.withCertificateAuthentication(..)` are gone, with no deprecation period.
18.x is the last version that signs requests. `DigipostClient.withJwtMtlsAuthentication(..)`
was renamed to `DigipostClient.create(..)` at the same time, as it is now the only way to
build a client.

To migrate, register a client with the
Digipost OAuth 2 client authority to get a client ID,
and replace the `Signer` with a `JwtAuthConfig`:

```java
// Before (18.x)
Signer signer;
try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("certificate.p12"))) {
    signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, "TheSecretPassword");
}

DigipostClient client = DigipostClient.withCertificateAuthentication(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), signer);

// After (19.x)
JwtAuthConfig jwtAuthConfig;
try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("client-cert.p12"))) {
    jwtAuthConfig = JwtAuthConfig
            .newConfig("your-client-id")
            .pkcs12KeyStore(sertifikatInputStream, "TheSecretPassword")
            .build();
}

DigipostClient client = DigipostClient.create(
        DigipostClientConfig.newConfiguration().build(), senderId.asBrokerId(), jwtAuthConfig);
```

Note that the `.p12` is generally **not** the same file. It is no longer a signing key for
your requests, but the client certificate presented in the mTLS handshake against the token
endpoint, and it is issued when you register the client.

Nothing else changes. The server still signs its responses and the client still verifies
them, so your `DigipostClientConfig`, message building and delivery code are untouched.
