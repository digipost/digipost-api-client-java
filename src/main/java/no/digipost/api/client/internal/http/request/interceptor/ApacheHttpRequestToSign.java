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
package no.digipost.api.client.internal.http.request.interceptor;

import no.digipost.api.client.security.RequestToSign;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;

import java.net.URISyntaxException;
import java.util.SortedMap;
import java.util.TreeMap;

final class ApacheHttpRequestToSign implements RequestToSign {

    private final HttpRequest clientRequest;

    public ApacheHttpRequestToSign(final HttpRequest httpRequest) {
        this.clientRequest = httpRequest;
    }

    @Override
    public String getMethod() {
        return clientRequest.getMethod();
    }

    @Override
    public SortedMap<String, String> getHeaders() {
        TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
        Header[] headers = clientRequest.getHeaders();
        for (Header header : headers) {
            sortedHeaders.put(header.getName(), header.getValue());
        }
        return sortedHeaders;
    }

    @Override
    public String getPath() {
        try {
            String path = clientRequest.getUri().getPath();
            return path != null ? path : "";
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String getParameters() {
        try {
            return queryParametersFromURI(clientRequest.getUri().toString());
        } catch (URISyntaxException e) {
            return "";
        }
    }

    static String queryParametersFromURI(String uri) {
        int index = uri.indexOf('?');

        return index == -1 ? "" : uri.substring(index + 1);
    }

}
