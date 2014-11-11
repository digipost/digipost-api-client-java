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

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import static javax.ws.rs.core.HttpHeaders.DATE;
import static no.digipost.api.client.Headers.Content_MD5;
import static no.digipost.api.client.Headers.X_Content_SHA256;
import static no.digipost.api.client.Headers.X_Digipost_UserId;

public class ResponseMessageSignatureUtil {

	private static final List<String> HEADERS_FOR_SIGNATURE = Arrays.asList(Content_MD5.toLowerCase(), DATE.toLowerCase(),
			X_Digipost_UserId.toLowerCase(), X_Content_SHA256.toLowerCase());

	public static String getCanonicalResponseRepresentation(final ClientResponseToVerify clientResponseToVerify) {
		StringBuilder s = new StringBuilder();
		s.append(getCanonicalResponseCodeRepresentation(clientResponseToVerify));
		s.append(getCanonicalUrlRepresentation(clientResponseToVerify));
		s.append(getCanonicalHeaderRepresentation(clientResponseToVerify));
		return s.toString();
	}

	private static String getCanonicalResponseCodeRepresentation(final ClientResponseToVerify clientResponseToVerify) {
		return clientResponseToVerify.getStatus() + "\n";
	}

	private static String getCanonicalUrlRepresentation(final ClientResponseToVerify clientResponseToVerify) {
		return clientResponseToVerify.getPath().toLowerCase() + "\n";
	}

	private static String getCanonicalHeaderRepresentation(final ClientResponseToVerify clientResponseToVerify) {
		SortedMap<String, String> headers = clientResponseToVerify.getHeaders();
		StringBuilder headersString = new StringBuilder();
		for (Entry<String, String> entry : headers.entrySet()) {
			String key = entry.getKey();
			if (isHeaderForSignature(key)) {
				headersString.append(key.toLowerCase() + ": " + entry.getValue() + "\n");
			}
		}
		return headersString.toString();
	}

	private static boolean isHeaderForSignature(final String key) {
		return HEADERS_FOR_SIGNATURE.contains(key.toLowerCase());
	}

}
