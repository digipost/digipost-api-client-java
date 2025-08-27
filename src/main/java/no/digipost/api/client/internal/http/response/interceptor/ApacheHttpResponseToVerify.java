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
package no.digipost.api.client.internal.http.response.interceptor;

import no.digipost.api.client.security.ResponseToVerify;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;

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
        return response.getCode();
    }

    @Override
    public SortedMap<String, String> getHeaders() {
        SortedMap<String, String> sortedHeaders = new TreeMap<>();
        for(Header header : response.getHeaders()){
            sortedHeaders.put(header.getName(), header.getValue());
        }
        return sortedHeaders;
    }

    @Override
    public String getPath() {
        String pathWithQueryParams = (String) context.getAttribute("request-path");
        int indexOfQuestionMark = pathWithQueryParams.indexOf('?');
        if (indexOfQuestionMark != -1) {
           return pathWithQueryParams.substring(0, indexOfQuestionMark);
        }
        return pathWithQueryParams;
    }
}
