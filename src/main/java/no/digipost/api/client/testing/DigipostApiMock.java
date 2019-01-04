package no.digipost.api.client.testing;
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

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.internal.http.Headers;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.sender.SenderFeature;
import no.digipost.api.client.representations.sender.SenderFeatureName;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.representations.sender.SenderStatus;
import no.digipost.api.client.util.DateUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.EnumUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.security.*;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import static io.undertow.util.Headers.*;
import static java.lang.Integer.parseInt;
import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static no.digipost.api.client.representations.MessageStatus.COMPLETE;
import static no.digipost.api.client.util.JAXBContextUtils.*;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.http.HttpStatus.SC_OK;

public class DigipostApiMock implements HttpHandler {

    public enum Method {
        SEND_MULTIPART_MESSAGE,
        GET_PRINT_KEY,
        GET_SENDER_INFORMATION,
        GET_CONTENT,
        MULTIPART_MESSAGE,
        GET_DOCUMENTS_EVENTS,
        GET_DOCUMENT_STATUS,
        IDENTIFY_RECIPIENT,
    }

    private static final Logger LOG = LoggerFactory.getLogger(DigipostApiMock.class);

    private static final String PRINT_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA+kcLpddPWKYlmIPZSDtw\n" +
            "9B/k+PFcCqnjh2xdhSkmYh+FQsYts0U1WCA8s7NPDUM7qwa97jlQ+la5AAxprfJQ\n" +
            "YOAp6NI5yLRuiah3EdnbPOyTQJ/C7hiw0P1lK0301BEMB2oHIxGYjUTpGeLLm2tH\n" +
            "z9VfJzkefCq1W/KV63AIDWDopwnw8UdhOgtB3BY+fVWVsv8WtG3kbfVn7t+7A5M9\n" +
            "ueE0BGGzL7XHCwBHJDq8+D0qNNvKIjp6KSgb+ZaREQEgCujuL77Qtm+6zKCpJXi2\n" +
            "yF+7mEaHj5k4pSnzI/N7CyDK1s24CeRpfDT0o8dz2O0VmV1ukA7wAqVJk2tKlhqE\n" +
            "IwIDAQAB\n" +
            "-----END PUBLIC KEY-----\n";

    private static final String PRINT_ID = "81bc03451048fba4d31f8";

    private static final String certificate = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDczCCAlugAwIBAgIEfMS/vzANBgkqhkiG9w0BAQsFADBqMQswCQYDVQQGEwJO\n" +
            "TzENMAsGA1UECBMET3NsbzENMAsGA1UEBxMET3NsbzERMA8GA1UEChMIRGlnaXBv\n" +
            "c3QxEDAOBgNVBAsTB0RpZ2lwb3MxGDAWBgNVBAMTD1NpbW9uIEFuZGVyc3NvbjAe\n" +
            "Fw0xNjA2MTMxMDE5MTJaFw0xNjA5MTExMDE5MTJaMGoxCzAJBgNVBAYTAk5PMQ0w\n" +
            "CwYDVQQIEwRPc2xvMQ0wCwYDVQQHEwRPc2xvMREwDwYDVQQKEwhEaWdpcG9zdDEQ\n" +
            "MA4GA1UECxMHRGlnaXBvczEYMBYGA1UEAxMPU2ltb24gQW5kZXJzc29uMIIBIjAN\n" +
            "BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgTSx+3/K/AOpG1TGcnceq6H1FNp1\n" +
            "VseUULBFhT7wmodEhijI5RPOkG0Okt0GA6yT6BNS7xhPrxLROSnFPCURtq/h13Gf\n" +
            "O/5DKUqoofIzP2f26s96GNwlUHLTnnGQ1mq2LTVu8Hi3vXwXjeBWoHSVceF8nPZr\n" +
            "YWm4Cxqn09dxWv/FuOqX1JwLwWBGgkyONohdmFaHFg+MRHzArvkdBM8sleD8Cwyx\n" +
            "ASlB7+9ttVZrssg2IxZ6wBEbkulGjLOrOa//dxwlFpEww9pm89hRIdj9sRUScbCO\n" +
            "Hto7KUMbbhZgiaDvCjIfOVeGWCJKyMF7avPk/86z11F5rjF33JieItn6RwIDAQAB\n" +
            "oyEwHzAdBgNVHQ4EFgQU/bPHm6m6ISWYU0DOUw8KqjEF8w4wDQYJKoZIhvcNAQEL\n" +
            "BQADggEBAEj05cTCWZuzhjKyOx2SvLc40WHPkTZCLx+4LNZHdckKgdGMcJJfqYr+\n" +
            "eTbsdzBgmFa1wMO0H6fKcaEgiJj2G9B4no/7bUWY2q3I21ZprJpiJnvuk36blcw1\n" +
            "s7jaDFQ758fyqAV/Sqqz91gSKPRsKCtcHC72XEqdc40qcecY1+3OVg5rADJHC8u+\n" +
            "QaXy0670ViiULi3vmQNAtD28DZcOHbbplVXKSyVifnALTrCQBYMqQF8GpKs2NcId\n" +
            "0XUU7HFKXmYESXGY4sW77+l8mRXLcpD/ofAszblUPpPCFFWozH4/WoY7tDRDhwkz\n" +
            "6vYEpI930hzj81irYEPyRvoDAaEv2Zc=\n" +
            "-----END CERTIFICATE-----";

    private final Clock clock;
    public final CloseableHttpResponse defaultResponse;
    private int port;
    private Undertow server;
    private Map<Method, RequestsAndResponses> requestsAndResponsesMap;
    private KeyPair keyPair;

    public DigipostApiMock() {
        this(Clock.systemDefaultZone());
    }

    public DigipostApiMock(Clock clock) {
        this.clock = clock;
        this.defaultResponse = getDefaultResponse(clock);
    }

    @Override
    public void handleRequest(HttpServerExchange httpContext) throws Exception {
        int httpResponse = 200;
        HttpString method = httpContext.getRequestMethod();
        String requestPath = httpContext.getRequestPath().toLowerCase();

        ByteArrayOutputStream bao = new ByteArrayOutputStream();

        if (requestPath.equals("/identification")) {
            httpResponse = serviceIdentifyRecipient(httpContext, bao);

        } else if (method.equals(new HttpString("POST"))){
            httpResponse = serviceMultipartrequest(httpContext, bao);

        } else if(requestPath.equals("/printkey")) {
            httpResponse = servicePrintKey(httpContext, bao);

        } else if(requestPath.equals("/getsenderinformation")) {
            serviceSenderInformation(httpContext, bao);

        } else if(requestPath.equals("/getdocumentstatus")) {
            CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_DOCUMENT_STATUS).getResponse(httpContext.getRequestPath());
            httpResponse = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.writeTo(bao);
            }
        } else if(requestPath.equals("/getdocumentevents")) {
            CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_DOCUMENTS_EVENTS).getResponse(httpContext.getRequestPath());
            httpResponse = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.writeTo(bao);
            }
        } else if(requestPath.equals("/")){
            marshal(jaxbContext, new EntryPoint(certificate, new Link(Relation.CREATE_MESSAGE, new DigipostUri("http://localhost:9999/create")),
                    new Link(Relation.GET_PRINT_ENCRYPTION_KEY, new DigipostUri("http://localhost:9999/printkey")),
                    new Link(Relation.GET_SENDER_INFORMATION, new DigipostUri("http://localhost:9999/getsenderinformation")),
                    new Link(Relation.DOCUMENT_EVENTS, new DigipostUri("http://localhost:9999/getdocumentevents")),
                    new Link(Relation.IDENTIFY_RECIPIENT, new DigipostUri("http://localhost:9999/identification"))), bao);

        } else {
            CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_CONTENT).getResponse(httpContext.getRequestPath());
            httpResponse = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                entity.writeTo(bao);
            }
        }

        byte[] bytes = bao.toByteArray();
        HeaderMap responseHeaders = httpContext.getResponseHeaders();

        String dateOnRFC1123Format = DateUtils.formatDate(now(clock));
        String xContentSHA256 = generateXContentSHA256(bytes);
        String signature =  httpResponse + "\n" + httpContext.getRequestPath().toLowerCase() + "\n" +
                "date: " + dateOnRFC1123Format + "\n" + "x-content-sha256: " + xContentSHA256 + "\n";

        responseHeaders.add(new HttpString("Date"), dateOnRFC1123Format);
        responseHeaders.add(new HttpString(Headers.X_Content_SHA256), xContentSHA256);
        responseHeaders.add(new HttpString(Headers.X_Digipost_Signature), generateXDigipostSignature(keyPair, signature));

        httpContext.setStatusCode(httpResponse);

        httpContext.startBlocking();
        IOUtils.copy(new ByteArrayInputStream(bytes), httpContext.getOutputStream());
    }

    private int serviceIdentifyRecipient(HttpServerExchange httpContext, ByteArrayOutputStream bao) throws IOException {
        CloseableHttpResponse response = requestsAndResponsesMap.get(Method.IDENTIFY_RECIPIENT).getResponse(httpContext.getRequestPath());
        if (response == null) {
            marshal(jaxbContext, IdentificationResult.digipost("test.testsson#1234"), bao);
            return 200;
        } else {
            response.getEntity().writeTo(bao);
            return response.getStatusLine().getStatusCode();
        }
    }

    private int serviceSenderInformation(HttpServerExchange httpContext, ByteArrayOutputStream bao) throws IOException {
        CloseableHttpResponse response = requestsAndResponsesMap.get(Method.GET_SENDER_INFORMATION).getResponse(httpContext.getRequestPath());
        if(response == null) {
            List<SenderFeature> senderFeatures = new ArrayList<>();
            senderFeatures.add(SenderFeatureName.DELIVERY_DIRECT_TO_PRINT.withNoParam());
            senderFeatures.add(SenderFeatureName.DIGIPOST_DELIVERY.withNoParam());
            senderFeatures.add(SenderFeatureName.PRINTVALIDATION_FONTS.withNoParam());
            senderFeatures.add(SenderFeatureName.PRINTVALIDATION_PDFVERSION.withNoParam());
            marshal(jaxbContext,
                    new SenderInformation(9999L, SenderStatus.VALID_SENDER, senderFeatures), bao);

            return 200;
        } else {
            response.getEntity().writeTo(bao);
            return response.getStatusLine().getStatusCode();
        }
    }

    private int servicePrintKey(HttpServerExchange httpContext, ByteArrayOutputStream bao){
        requestsAndResponsesMap.get(Method.GET_PRINT_KEY).getResponse(httpContext.getRequestPath());
        EncryptionKey encryptionKey = new EncryptionKey();
        encryptionKey.setKeyId(PRINT_ID);
        encryptionKey.setValue(PRINT_KEY);
        marshal(jaxbContext, encryptionKey, bao);

        return 200;
    }

    private int serviceMultipartrequest(HttpServerExchange httpContext, ByteArrayOutputStream bao) throws IOException {
        HeaderMap requestHeaders = httpContext.getRequestHeaders();
        String digipostMultipartContentType = requestHeaders.getFirst(CONTENT_TYPE);
        String boundary = extractQuotedValueFromHeader(digipostMultipartContentType, "boundary");
        requestHeaders.addFirst(CONTENT_TYPE, MultiPartParserDefinition.MULTIPART_FORM_DATA + "; boundary=" + boundary);
        FormData parsedMultipart = FormParserFactory.builder()
                .addParser(new MultiPartParserDefinition())
                .build()
                .createParser(httpContext)
                .parseBlocking();
        Map<String, ContentPart> parts = stream(parsedMultipart.spliterator(), false)
            .flatMap(partName -> parsedMultipart.get(partName).stream())
            .map(ContentPart::new)
            .collect(toMap(ContentPart::getName, Function.identity(),
                    (part1WithDuplicateName, part2WithDuplicateName) -> {
                        throw new RuntimeException("Found two parts with same name: " + part1WithDuplicateName + ", " + part2WithDuplicateName);
                    },
                    LinkedHashMap::new));


        String messageString = Optional.ofNullable(parts.remove("message"))
                .orElseThrow(() -> new NoSuchElementException("Message part of multipart not found in " + parts))
                .content;

        Message message;
        try (InputStream messageStream = new ByteArrayInputStream(messageString.getBytes())) {
            message = unmarshal(jaxbContext, messageStream, Message.class);
        }

        RequestsAndResponses requestsAndResponses = this.requestsAndResponsesMap.get(Method.MULTIPART_MESSAGE);
        CloseableHttpResponse response = requestsAndResponses.getResponse(message.primaryDocument.subject);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.writeTo(bao);
        }

        requestsAndResponses.addRequest(new DigipostRequest(message, new ArrayList<>(parts.values())));

        return response.getStatusLine().getStatusCode();
    }

    private static String generateXContentSHA256(byte[] bytes){
        SHA256Digest digest = new SHA256Digest();
        byte[] entityBytes = bytes;
        digest.update(entityBytes, 0, entityBytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);

        return new String(Base64.encode(result));
    }

    private static String generateXDigipostSignature(KeyPair keyPair, String signature) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
        instance.initSign(keyPair.getPrivate());
        instance.update(signature.getBytes());
        byte[] signedSignature = instance.sign();
        return new String(Base64.encode(signedSignature));
    }

    public DigipostApiMock start(int port, Map<Method, RequestsAndResponses> requestsAndResponsesMap,
                                 KeyPair keyPair) {
        this.keyPair = keyPair;
        this.requestsAndResponsesMap = requestsAndResponsesMap;
        this.port = port;
        this.server = Undertow.builder().addHttpListener(port, "localhost", new BlockingHandler(this)).build();
        init();
        server.start();
        LOG.info("Digipost API client running on port {}", port);
        return this;
    }

    public void stop() {
        server.stop();
        LOG.info("Shutting down Digipost API client mock on port {}", port);
    }

    public void init() {
        requestsAndResponsesMap.clear();
        requestsAndResponsesMap.put(Method.GET_CONTENT, new RequestsAndResponses());
        requestsAndResponsesMap.put(Method.GET_DOCUMENTS_EVENTS, new RequestsAndResponses());
        requestsAndResponsesMap.put(Method.GET_DOCUMENT_STATUS, new RequestsAndResponses());
        requestsAndResponsesMap.put(Method.GET_PRINT_KEY, new RequestsAndResponses());
        requestsAndResponsesMap.put(Method.GET_SENDER_INFORMATION, new RequestsAndResponses());
        requestsAndResponsesMap.put(Method.SEND_MULTIPART_MESSAGE, new RequestsAndResponses());
        requestsAndResponsesMap.put(Method.MULTIPART_MESSAGE, new RequestsAndResponses(new MultipartRequestMatcher()));
        requestsAndResponsesMap.put(Method.IDENTIFY_RECIPIENT, new RequestsAndResponses());
    }


    @FunctionalInterface
    public interface RequestMatcher {
        CloseableHttpResponse findResponse(String requestString);
        public static final RequestMatcher NO_OP = requestString -> null;
    }

    public static class RequestsAndResponses {
        private final Queue<ResponseProducer> responseQueue = new ConcurrentLinkedQueue<>();
        private final RequestMatcher requestMatcher;
        private final Map<String, MockRequest> requestMap;

        RequestsAndResponses() {
            this(RequestMatcher.NO_OP);
        }

        RequestsAndResponses(RequestMatcher requestMatcher) {
            this.requestMatcher = requestMatcher;
            this.requestMap = Collections.synchronizedMap(new LinkedHashMap<String, MockRequest>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, MockRequest> eldest) {
                    return size() > 100;
                }
            });
        }

        public void addExpectedResponse(CloseableHttpResponse response) {
            responseQueue.add(new MockResponse(response));
        }
        public void addExpectedException(RuntimeException ex) {
            responseQueue.add(new MockResponse(ex));
        }

        public CloseableHttpResponse getResponse() {
            return getResponse("default");
        }

        public CloseableHttpResponse getResponse(String requestString) {
            ResponseProducer response = responseQueue.poll();
            if (response != null) {
                return response.getResponse();
            }

            return requestMatcher.findResponse(requestString);
        }

        public void addRequest(MockRequest request) {
            requestMap.put(request.getKey(), request);
        }

        public MockRequest getRequest(String requestKey) {
            return requestMap.get(requestKey);
        }

        public Map<String, MockRequest> getRequests() {
            return requestMap;
        }
    }

    public static interface ResponseProducer {
        CloseableHttpResponse getResponse();
    }
    public static class MockResponse implements ResponseProducer {

        private CloseableHttpResponse response;
        private RuntimeException exception;

        public MockResponse(CloseableHttpResponse response) {
            this.response = response;
        }
        public MockResponse(RuntimeException ex) {
            this.exception = ex;
        }

        @Override
        public CloseableHttpResponse getResponse() {
            if (exception != null) {
                throw exception;
            }
            return response;
        }
    }

    public static class MockRequest {
        private final String key;

        public MockRequest(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }



    public static CloseableHttpResponse getDefaultResponse(Clock clock) {
        MessageDelivery messageDelivery = new MessageDelivery(UUID.randomUUID().toString(), Channel.DIGIPOST, COMPLETE, now(clock));

        org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
        marshal(jaxbContext, messageDelivery, bao);

        return MockfriendlyResponse.MockedResponseBuilder.create()
                .status(SC_OK)
                .entity(new ByteArrayEntity(bao.toByteArray()))
                .build();
    }
    public class MultipartRequestMatcher implements RequestMatcher {

        public final Map<String, CloseableHttpResponse> responses = new HashMap<>();
        public final Map<String, RuntimeException> errors = new HashMap<>();

        public MultipartRequestMatcher() {
            responses.put("200:OK", defaultResponse);
            errors.put("CONNECTION_REFUSED", new RuntimeException(new ConnectException("Connection refused")));
        }


        @Override
        public CloseableHttpResponse findResponse(String requestString) {
            if (responses.containsKey(requestString)) {
                return responses.get(requestString);
            } else if (errors.containsKey(requestString)) {
                throw errors.get(requestString);
            } else if (requestString.matches("^[0-9]{3}:(.)+")) {
                String[] split = requestString.split(":");
                if (ErrorCode.isKnown(split[1])) {
                    ErrorCode errorCode = ErrorCode.resolve(split[1]);
                    ErrorType translated = EnumUtils.getEnum(ErrorType.class, errorCode.getOverriddenErrorType().name());

                    org.apache.commons.io.output.ByteArrayOutputStream bao = new org.apache.commons.io.output.ByteArrayOutputStream();
                    marshal(jaxbContext,
                            new ErrorMessage(translated != null ? translated : ErrorType.SERVER, errorCode.name(),
                                    "Generic error-message from digipost-api-client-mock"), bao);

                    return MockfriendlyResponse.MockedResponseBuilder.create().status(parseInt(split[0]))
                            .entity(new ByteArrayEntity(bao.toByteArray())).build();
                } else {
                    throw new IllegalArgumentException("ErrorCode " + split[1] + " is unknown");
                }
            } else {
                return defaultResponse;
            }

        }
    }

    public static class DigipostRequest extends MockRequest {
        public final Message message;
        public final List<ContentPart> contentParts;

        public DigipostRequest(Message message, List<ContentPart> contentParts) {
            super(message.messageId);
            this.message = message;
            this.contentParts = contentParts;
        }

    }

    public static final class ContentPart {

        public final String name;
        public final String mediaType;
        public final String content;

        public ContentPart(FormValue part) {
            this(extractQuotedValueFromHeader(part.getHeaders().getFirst(CONTENT_DISPOSITION), "filename"), part.getHeaders().get(CONTENT_TYPE).getFirst(), part.getValue());
        }

        public ContentPart(String name, String mediaType, String content) {
            this.name = name;
            this.mediaType = mediaType;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Content '" + name + "' (" + mediaType + "): " + abbreviate(content, 15);
        }
    }
}
