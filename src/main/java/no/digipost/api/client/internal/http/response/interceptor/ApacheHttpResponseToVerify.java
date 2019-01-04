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
package no.digipost.api.client.internal.http.response.interceptor;

import no.digipost.api.client.security.ResponseToVerify;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import java.util.SortedMap;
import java.util.TreeMap;

final class ApacheHttpResponseToVerify implements ResponseToVerify {

    private final HttpContext context;
    private final HttpResponse response;

    public ApacheHttpResponseToVerify(final HttpContext context, final HttpResponse response) {
        this.context = context;
        this.response = response;
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public SortedMap<String, String> getHeaders() {
        SortedMap<String, String> sortedHeaders = new TreeMap<>();
        for(Header header : response.getAllHeaders()){
            sortedHeaders.put(header.getName(), header.getValue());
        }
        return sortedHeaders;
    }

    @Override
    public String getPath() {
        return (String) context.getAttribute("request-path");
    }
}
