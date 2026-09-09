/*
 * Copyright (C) Posten Bring AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.api.client.security.jwt;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.WWWFormCodec;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A local HTTPS server standing in for the OAuth 2.0 token endpoint, presenting a
 * generated certificate valid for 127.0.0.1 and requesting a client certificate.
 */
final class TokenEndpointStub implements Closeable {

    private final HttpsServer server;
    private final X509Certificate serverCertificate;
    private final URI tokenEndpointUri;

    private final List<List<NameValuePair>> receivedForms = new ArrayList<>();
    private final AtomicReference<Certificate[]> certificatesPresentedByClient = new AtomicReference<>();

    private volatile int responseStatus = 200;
    private volatile String responseBody = "{}";

    TokenEndpointStub() throws Exception {
        KeyPair keyPair = generateKeyPair();
        this.serverCertificate = selfSignedCertificateFor(keyPair);

        server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        SSLContext serverContext = serverSslContext(keyPair, serverCertificate);
        server.setHttpsConfigurator(new HttpsConfigurator(serverContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLParameters sslParameters = serverContext.getDefaultSSLParameters();
                // TLS 1.3 defers client authentication past the handshake, which would leave
                // getPeerCertificates() empty in the handler below.
                sslParameters.setProtocols(new String[]{ "TLSv1.2" });
                sslParameters.setWantClientAuth(true);
                params.setSSLParameters(sslParameters);
            }
        });
        server.createContext("/token", exchange -> {
            try {
                certificatesPresentedByClient.set(((HttpsExchange) exchange).getSSLSession().getPeerCertificates());
            } catch (SSLPeerUnverifiedException e) {
                certificatesPresentedByClient.set(null);
            }
            String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            synchronized (receivedForms) {
                receivedForms.add(WWWFormCodec.parse(form, StandardCharsets.UTF_8));
            }

            String body = responseBody;
            if (body == null) {
                exchange.sendResponseHeaders(responseStatus, -1);
            } else {
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(responseStatus, bodyBytes.length);
                exchange.getResponseBody().write(bodyBytes);
            }
            exchange.close();
        });
        server.start();

        this.tokenEndpointUri = URI.create("https://127.0.0.1:" + server.getAddress().getPort() + "/token");
    }

    URI tokenEndpointUri() {
        return tokenEndpointUri;
    }

    void respondWith(int status, String body) {
        this.responseStatus = status;
        this.responseBody = body;
    }

    /** Respond with the given status and no response body at all, i.e. not even an empty one. */
    void respondWithoutBody(int status) {
        this.responseStatus = status;
        this.responseBody = null;
    }

    int receivedRequestCount() {
        synchronized (receivedForms) {
            return receivedForms.size();
        }
    }

    List<NameValuePair> lastReceivedForm() {
        synchronized (receivedForms) {
            return receivedForms.get(receivedForms.size() - 1);
        }
    }

    Certificate[] certificatesPresentedByClient() {
        return certificatesPresentedByClient.get();
    }

    /** Trust managers accepting this stub's certificate, in place of the JVM default trust store. */
    TrustManager[] trustManagers() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("token-endpoint", serverCertificate);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static SSLContext serverSslContext(KeyPair keyPair, X509Certificate certificate) throws Exception {
        char[] password = "token-endpoint-stub".toCharArray();

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setKeyEntry("token-endpoint", keyPair.getPrivate(), password, new Certificate[]{ certificate });

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), anyClientCertificate(), null);
        return sslContext;
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static X509Certificate selfSignedCertificateFor(KeyPair keyPair) throws Exception {
        X500Name subject = new X500Name("CN=token-endpoint-stub");
        Date notBefore = new Date(System.currentTimeMillis() - 86400_000);
        Date notAfter = new Date(System.currentTimeMillis() + 86400_000);

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject, BigInteger.ONE, notBefore, notAfter, subject, keyPair.getPublic());
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        builder.addExtension(Extension.subjectAlternativeName, false,
                new GeneralNames(new GeneralName(GeneralName.iPAddress, "127.0.0.1")));

        return new JcaX509CertificateConverter().getCertificate(
                builder.build(new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate())));
    }

    private static TrustManager[] anyClientCertificate() {
        return new TrustManager[]{ new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        } };
    }
}
