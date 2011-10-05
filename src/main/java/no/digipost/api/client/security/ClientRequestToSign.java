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

import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.core.MultivaluedMap;


import com.sun.jersey.api.client.ClientRequest;

public class ClientRequestToSign implements RequestToSign {

	private final ClientRequest clientRequest;

	public ClientRequestToSign(final ClientRequest clientRequest) {
		this.clientRequest = clientRequest;
	}

	@Override
	public String getMethod() {
		return clientRequest.getMethod();
	}

	@Override
	public SortedMap<String, String> getHeaders() {
		TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
		MultivaluedMap<String, Object> headers = clientRequest.getHeaders();
		for (String key : headers.keySet()) {
			sortedHeaders.put(key, ClientRequest.getHeaderValue(headers.getFirst(key)));
		}
		return sortedHeaders;
	}

	@Override
	public String getPath() {
		String path = clientRequest.getURI().getRawPath();
		return path != null ? path : "";
	}

	@Override
	public String getParameters() {
		String query = clientRequest.getURI().getRawQuery();
		return query != null ? query : "";
	}

}
