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
package no.digipost.api.client.util;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public class LoggingUtil {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingUtil.class);

	public static void logResponse(HttpResponse response) {
		LOG.info("HTTP response status code: {}", response.getStatusLine().getStatusCode());
		LOG.info("HTTP response headers: {}", headersAsString(response.getAllHeaders()));
	}

	public static void logResponse(ClientResponseContext clientResponseContext) {
		LOG.info("HTTP response status code: {}", clientResponseContext.getStatus());
		LOG.info("HTTP response headers: {}", headersAsString(clientResponseContext.getHeaders()));
	}

	public static String headersAsString(Header[] httpHeaders) {

		StringBuffer result = new StringBuffer();

		for (Header header : httpHeaders) {

			result.append(header.getName()).append(" :");

			for (HeaderElement element : header.getElements()) {
				result.append(" ").append(element.getValue());
			}

			result.append("; ");

		}
		return result.toString();

	}

	public static String headersAsString(MultivaluedMap<String, String> httpHeaders) {

		StringBuffer result = new StringBuffer();

		for (String key : httpHeaders.keySet()) {

			result.append(key).append(" :");

			List<String> values = httpHeaders.get(key);
			for (String value : values) {
				result.append(" ").append(value);
			}

			result.append("; ");

		}
		return result.toString();

	}

}
