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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class ReceivedRequest {
	public final String path;
	public final String queryParams;
	public final String method;
	public final Map<String, String> headers;
	private final String body;

	public ReceivedRequest(String method, String path, String queryParams, Map<String, String> headers, String postData) {
		this.method = method;
		this.path = path;
		this.queryParams = queryParams;
		this.headers = unmodifiableMap(headers);
		this.body = postData;
	}

	@Override
	public String toString() {
		return method + " " + path + queryParams; //ofNullable(queryParams).map(s -> "?" + s).orElse("");
	}

	public String getQueryParamValueFor(String paramName) {
		/*return Stream.of(queryParams.split("&"))
				.map(keyValue -> Tuple.of(keyValue, keyValue.indexOf('=')))
				.map(keyValueAndDelimPos -> Tuple.of(
						keyValueAndDelimPos.first().substring(0, keyValueAndDelimPos.second()),
						keyValueAndDelimPos.first().substring(keyValueAndDelimPos.second() + 1)))
				.filter(t -> paramName.equals(t.first()))
				.findAny()
				.map(Tuple::second)
				.orElseThrow(() -> new IllegalStateException("No " + paramName + " query param found in " + this));*/
		return "";
	}

	public InputStream getBody() {
		return new ByteArrayInputStream(body.getBytes());
	}

	public String getBodyAsString() {
		return body;
	}
}
