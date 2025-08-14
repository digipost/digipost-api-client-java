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
package no.digipost.api.client.internal.http;


import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentTooLongException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class MultipartNoLengthCheckHttpEntity implements HttpEntity {
    private final HttpEntity entity;

    public MultipartNoLengthCheckHttpEntity(HttpEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean isRepeatable() {
        return entity.isRepeatable();
    }

    @Override
    public boolean isChunked() {
        return entity.isChunked();
    }

    @Override
    public long getContentLength() {
        return entity.getContentLength();
    }

    @Override
    public String getContentType() {
        return entity.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return entity.getContentEncoding();
    }

    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        if (entity.getContentLength() < 0) {
            throw new ContentTooLongException("Content length is unknown");
        }
        final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        writeTo(outstream);
        outstream.flush();
        return new ByteArrayInputStream(outstream.toByteArray());
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        entity.writeTo(outputStream);
    }

    @Override
    public boolean isStreaming() {
        return entity.isStreaming();
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public final void close() throws IOException {
        entity.close();
    }
}
