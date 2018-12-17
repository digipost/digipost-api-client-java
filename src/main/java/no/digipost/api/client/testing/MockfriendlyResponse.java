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

import no.digipost.api.client.representations.MessageDelivery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;

import javax.xml.bind.JAXB;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.MessageStatus.COMPLETE;
import static org.apache.http.HttpStatus.SC_OK;

public class MockfriendlyResponse implements CloseableHttpResponse {

    public static final Map<String, CloseableHttpResponse> responses = new HashMap<>();
    public static final Map<String, RuntimeException> errors = new HashMap<>();

    public static CloseableHttpResponse DEFAULT_RESPONSE = getDefaultResponse();

    public static CloseableHttpResponse getDefaultResponse(){
        MessageDelivery messageDelivery = new MessageDelivery(UUID.randomUUID().toString(), DIGIPOST, COMPLETE, ZonedDateTime.now());
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        JAXB.marshal(messageDelivery, bao);

        return MockedResponseBuilder.create()
                .status(HttpStatus.SC_OK)
                .entity(new ByteArrayEntity(bao.toByteArray()))
                .build();
    }

    public static RuntimeException CONNECTION_REFUSED = new RuntimeException(new ConnectException("Connection refused"));

    static {
        responses.put("200:OK", DEFAULT_RESPONSE);
        errors.put("CONNECTION_REFUSED", CONNECTION_REFUSED);
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public StatusLine getStatusLine() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public HttpEntity getEntity() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setEntity(HttpEntity entity) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public Header[] getHeaders(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public Header getFirstHeader(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public Header getLastHeader(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public Header[] getAllHeaders() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setHeaders(Header[] headers) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void removeHeader(Header header) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void removeHeaders(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public HeaderIterator headerIterator() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public HeaderIterator headerIterator(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpParams getParams() {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setParams(@SuppressWarnings("deprecation") HttpParams params) {
        throw new UnsupportedOperationException("This is a mock");
    }

    public static class MockedResponseBuilder {
        private int status;
        private HttpEntity entity;

        public static MockedResponseBuilder create() {
            return new MockedResponseBuilder();
        }

        public MockedResponseBuilder status(final int status) {
            this.status = status;
            return this;
        }

        public MockedResponseBuilder entity(final HttpEntity entity) {
            this.entity = entity;
            return this;
        }

        public MockedResponseBuilder entity(final Object object) {
            try {
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                if(object instanceof InputStream){
                    IOUtils.copy((InputStream)object, bao);
                }
                else {
                    JAXB.marshal(object, bao);
                }

                this.entity = new ByteArrayEntity(bao.toByteArray());
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public MockfriendlyResponse build() {
            return new MockfriendlyResponse() {
                @Override
                public StatusLine getStatusLine() {
                    return new BasicStatusLine(new ProtocolVersion("1",2,3), status, "reason");
                }

                @Override
                public HttpEntity getEntity() {
                    return entity;
                }

                @Override
                public void close() throws IOException {
                }
            };
        }

        public static CloseableHttpResponse ok(Object object) {
            try{
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                if(object instanceof InputStream){
                    IOUtils.copy((InputStream)object, bao);
                }
                else {
                    JAXB.marshal(object, bao);
                }
                return MockedResponseBuilder.create().status(SC_OK).entity(new ByteArrayEntity(bao.toByteArray())).build();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
