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
package no.digipost.api.client.filters.request;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.Headers;

import org.bouncycastle.crypto.digests.SHA256Digest;

import javax.ws.rs.ext.Provider;

@Provider
public class RequestContentSHA256Filter extends RequestContentHashFilter {
	public RequestContentSHA256Filter(final EventLogger eventListener) {
		super(eventListener, SHA256Digest.class, Headers.X_Content_SHA256);
	}

	public RequestContentSHA256Filter() {
		super(SHA256Digest.class, Headers.X_Content_SHA256);
	}
}
