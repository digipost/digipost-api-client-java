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
package no.digipost.api.client.representations;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;

public class DigipostUri {
	private final URI uri;

	public DigipostUri(final String... uriComponents) {
		try {
			uri = new URI(StringUtils.join(uriComponents, "/").replaceAll("([^:])/+", "$1/"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public DigipostUri(final URI uri) {
		this.uri = uri;
	}

	public URI getFullUri() {
		return uri;
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof DigipostUri) {
			return ((DigipostUri) obj).uri.equals(uri);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	public String getBaseUri() {
		String port = "";
		if (uri.getPort() != 80 && uri.getPort() != -1) {
			port = ":" + String.valueOf(uri.getPort());
		}

		return uri.getScheme() + "://" + uri.getHost() + port;
	}
}
