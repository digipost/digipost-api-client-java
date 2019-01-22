/**
 * Copyright (C) Posten Norge AS
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
package no.digipost.api.client.testing;

import no.digipost.api.client.BrokerId;
import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.testing.DigipostApiMock.Method;
import no.digipost.api.client.testing.DigipostApiMock.MockRequest;
import no.digipost.api.client.testing.DigipostApiMock.RequestsAndResponses;
import no.digipost.http.client3.DigipostHttpClientFactory;
import no.digipost.http.client3.DigipostHttpClientSettings;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.xml.sax.ContentHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static no.digipost.api.client.testing.UnusedPortFinder.getNextAvailablePort;

/**
 * Instansierer en DigipostClient som ikke går mot faktiskt Digipost REST-api endepunkt og
 * som kan brukes av tester o.l for å framprovosere ulike feilsituasjoner.
 */
public class DigipostClientMock {

    public final Map<Method, RequestsAndResponses> requestsAndResponsesMap = new HashMap<>();

    private final DigipostClient client;
    private final int port;
    private static final Clock clock = Clock.systemDefaultZone();
    private static final DigipostApiMock digipostApiMock = new DigipostApiMock(clock);
    private static final String KEY_STORE_PASSWORD = "Qwer12345";
    private static final String KEY_STORE_ALIAS = "apiTest";

    public DigipostClientMock() {
        this(UnaryOperator.identity());
    }

    public DigipostClientMock(UnaryOperator<DigipostHttpClientSettings> clientCustomizer) {
        this.port = getNextAvailablePort(6666, 6676);
        URI host = URI.create("http://localhost:" + port);

        HttpClientBuilder httpClientBuilder = DigipostHttpClientFactory.createBuilder(clientCustomizer.apply(DigipostHttpClientSettings.DEFAULT));
        DigipostClientConfig config = DigipostClientConfig.newConfiguration().clock(clock).digipostApiUri(host).build();
        this.client = new DigipostClient(config, BrokerId.of(1), data -> new byte[0], httpClientBuilder);
    }

    public void start() {
        KeyPair keyPair = getKeyPair(KEY_STORE_ALIAS, KEY_STORE_PASSWORD);
        digipostApiMock.start(port, requestsAndResponsesMap, keyPair);
    }

    public static KeyPair getKeyPair(final String alias, final String password) {
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(DigipostClientMock.class.getResourceAsStream("/mockKeystore.jks"), KEY_STORE_PASSWORD.toCharArray());

            Key key = keystore.getKey(alias, password.toCharArray());
            Certificate cert = keystore.getCertificate(alias);
            PublicKey publicKey = cert.getPublicKey();

            return new KeyPair(publicKey, (PrivateKey) key);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void shutdownWebserver() {
        digipostApiMock.stop();
    }

    public DigipostClient getClient() {
        return client;
    }

    public Map<String, MockRequest> getAllRequests(Method method) {
        return requestsAndResponsesMap.get(method).getRequests();
    }

    public MockRequest getRequest(Method method, String requestKey) {
        return requestsAndResponsesMap.get(method).getRequest(requestKey);
    }

    public void addExpectedResponse(Method method, CloseableHttpResponse response) {
        RequestsAndResponses requestsAndResponses = requestsAndResponsesMap.get(method);

        requestsAndResponses.addExpectedResponse(response);
    }

    public void addExpectedException(Method method, RuntimeException exception) {
        RequestsAndResponses requestsAndResponses = requestsAndResponsesMap.get(method);

        requestsAndResponses.addExpectedException(exception);
    }

    public void reset() {
        digipostApiMock.init();
    }


    /**
     * Threadsafe instance for marshalling and validating.
     */
    public static class ValidatingMarshaller {
        private final JAXBContext jaxbContext;
        private final Schema schema;

        public ValidatingMarshaller(JAXBContext jaxbContext, Schema schema) {
            this.jaxbContext = jaxbContext;
            this.schema = schema;
        }

        public void marshal(Object jaxbElement, ContentHandler handler) {
            Marshaller marshaller;
            try {
                marshaller = jaxbContext.createMarshaller();
                marshaller.setSchema(schema);
                marshaller.marshal(jaxbElement, handler);
            } catch (JAXBException e) {
                StringWriter w = new StringWriter();
                PrintWriter printWriter = new PrintWriter(w);
                e.printStackTrace(printWriter);
                throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST, "DigipostClientMock failed to marshall the " + jaxbElement.getClass().getSimpleName() + " to xml.\n\n" + w.toString());
            }
        }
    }

    /**
     * Used to validate that requests are according to XSD
     */
    public static ValidatingMarshaller initMarshaller() {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(DigipostClient.class.getResource("/xsd/api_v7.xsd"));
            return new ValidatingMarshaller(JAXBContext.newInstance("no.digipost.api.client.representations"), schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
