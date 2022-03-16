---
title: Instantiate and configure the client
identifier: client_config
layout: default
---

### Configure for production use

To instantiate the client instance you need to supply your assigned _broker ID_, which
is set up to be permitted to integrate with the Digipost API. In addition, you must create
a `Signer` instance, e.g. by using a `.p12` file to read the private key to use for signing
the API requests.

```java
SenderId senderId = SenderId.of(123456);

Signer signer;
try (InputStream sertifikatInputStream = Files.newInputStream(Paths.get("certificate.p12"))) {
    signer = Signer.usingKeyFromPKCS12KeyStore(sertifikatInputStream, "TheSecretPassword");
}

DigipostClient client = new DigipostClient(
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


