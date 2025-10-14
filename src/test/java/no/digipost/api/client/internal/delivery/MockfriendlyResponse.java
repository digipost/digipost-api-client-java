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
package no.digipost.api.client.internal.delivery;

import jakarta.xml.bind.JAXB;
import no.digipost.api.client.representations.MessageDelivery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V8;
import static no.digipost.api.client.representations.MessageStatus.COMPLETE;

public class MockfriendlyResponse implements ClassicHttpResponse {

    public static final Map<String, ClassicHttpResponse> responses = new HashMap<>();
    public static final Map<String, RuntimeException> errors = new HashMap<>();

    public static ClassicHttpResponse DEFAULT_RESPONSE = getDefaultResponse();

    public static ClassicHttpResponse getDefaultResponse() {
        MessageDelivery messageDelivery = new MessageDelivery(UUID.randomUUID().toString(), DIGIPOST, COMPLETE, ZonedDateTime.now());
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        JAXB.marshal(messageDelivery, bao);

        return MockedResponseBuilder.create()
                .status(HttpStatus.SC_OK)
                .entity(new ByteArrayEntity(bao.toByteArray(), ContentType.create(DIGIPOST_MEDIA_TYPE_V8)))
                .build();
    }

    public static RuntimeException CONNECTION_REFUSED = new RuntimeException(new ConnectException("Connection refused"));

    static {
        responses.put("200:OK", DEFAULT_RESPONSE);
        errors.put("CONNECTION_REFUSED", CONNECTION_REFUSED);
    }

    @Override
    public int getCode() {
        return 200;
    }

    @Override
    public void setCode(int code) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public String getReasonPhrase() {
        return "";
    }

    @Override
    public void close() throws IOException {
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
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public int countHeaders(String name) {
        return 0;
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
    public Header getHeader(String name) throws ProtocolException {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return new Header[0];
    }

    @Override
    public Header getLastHeader(String name) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public Iterator<Header> headerIterator() {
        return null;
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return null;
    }

    @Override
    public void setVersion(ProtocolVersion version) {

    }

    @Override
    public ProtocolVersion getVersion() {
        return null;
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void addHeader(String name, Object value) {

    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public void setHeader(String name, Object value) {

    }

    @Override
    public void setHeaders(Header ... headers) {
        throw new UnsupportedOperationException("This is a mock");
    }

    @Override
    public boolean removeHeader(Header header) {
        return false;
    }

    @Override
    public boolean removeHeaders(String name) {
        return false;
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

                this.entity = new ByteArrayEntity(bao.toByteArray(), ContentType.create(DIGIPOST_MEDIA_TYPE_V8));
                return this;
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public MockfriendlyResponse build() {
            return new MockfriendlyResponse() {
                @Override
                public HttpEntity getEntity() {
                    return entity;
                }

                @Override
                public int getCode() {
                    return status;
                }

                @Override
                public void close() throws IOException {
                }
            };
        }
    }
}
