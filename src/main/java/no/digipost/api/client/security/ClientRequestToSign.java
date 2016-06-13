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
package no.digipost.api.client.security;

import org.apache.http.Header;
import org.apache.http.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClientRequestToSign implements RequestToSign {

	private final HttpRequest clientRequest;

	public ClientRequestToSign(final HttpRequest httpRequest) {
		this.clientRequest = httpRequest;
	}

	@Override
	public String getMethod() {
		return clientRequest.getRequestLine().getMethod();
	}

	@Override
	public SortedMap<String, String> getHeaders() {
		TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
		Header[] headers = clientRequest.getAllHeaders();
		for (Header header : headers) {
			sortedHeaders.put(header.getName(), header.getValue());
		}
		return sortedHeaders;
	}

	@Override
	public String getPath() {
		try {
			String path = new URI(clientRequest.getRequestLine().getUri()).getPath();
			return path != null ? path : "";
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String getParameters() {
		int index = clientRequest.getRequestLine().getUri().indexOf('?');
		if(index == -1){
			return "";
		}
		String query = clientRequest.getRequestLine().getUri().substring(index + 1);
		return query != null ? query : "";
	}

}