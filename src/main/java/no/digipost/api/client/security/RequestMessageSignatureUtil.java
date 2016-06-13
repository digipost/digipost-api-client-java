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

import org.apache.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import static no.digipost.api.client.Headers.*;

public class RequestMessageSignatureUtil {

	private static final List<String> HEADERS_FOR_SIGNATURE = Arrays.asList(Content_MD5.toLowerCase(), HttpHeaders.DATE.toLowerCase(),
			X_Digipost_UserId.toLowerCase(), X_Content_SHA256.toLowerCase());

	public static String getCanonicalRequestRepresentation(final RequestToSign request) {
		StringBuilder s = new StringBuilder();
		s.append(getCanonicalMethodRepresentation(request));
		s.append(getCanonicalUrlRepresentation(request));
		s.append(getCanonicalHeaderRepresentation(request));
		s.append(getCanonicalParameterRepresentation(request));
		return s.toString();
	}

	private static String getCanonicalMethodRepresentation(final RequestToSign request) {
		return request.getMethod().toUpperCase() + "\n";
	}

	private static String getCanonicalUrlRepresentation(final RequestToSign request) {
		return request.getPath().toLowerCase() + "\n";
	}

	private static String getCanonicalHeaderRepresentation(final RequestToSign request) {
		SortedMap<String, String> headers = request.getHeaders();
		StringBuilder headersString = new StringBuilder();
		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			if (isHeaderForSignature(key)) {
				headersString.append(key.toLowerCase() + ": " + entry.getValue() + "\n");
			}
		}
		return headersString.toString();
	}

	private static String getCanonicalParameterRepresentation(final RequestToSign request) {
		return request.getParameters().toLowerCase() + "\n";
	}

	private static boolean isHeaderForSignature(final String key) {
		return HEADERS_FOR_SIGNATURE.contains(key.toLowerCase());
	}

}
