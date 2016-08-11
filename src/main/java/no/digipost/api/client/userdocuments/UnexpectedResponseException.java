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
package no.digipost.api.client.userdocuments;

import org.apache.http.StatusLine;

public class UnexpectedResponseException extends UserDocumentsApiException {
	private final Error error;
	private final String rawBody;

	public UnexpectedResponseException(final StatusLine status, final Error error) {
		super(String.format("Unexpected response: status [%s - %s], error [%s - %s]", status.getStatusCode(), status.getReasonPhrase(), error.getCode(), error.getMessage()));
		this.error = error;
		this.rawBody = null;
	}

	public UnexpectedResponseException(final StatusLine status, final Exception cause) {
		this(status, null, cause);
	}

	public UnexpectedResponseException(final StatusLine status, final String body) {
		this(status, body, null);
	}

	public UnexpectedResponseException(final StatusLine status, final String body, final Exception cause) {
		super(String.format("Unexpected response: status [%s - %s], response body [%s]", status.getStatusCode(), status.getReasonPhrase(), body), cause);
		this.error = null;
		this.rawBody = body;
	}

	public Error getError() {
		return error;
	}

	public String getRawBody() {
		return rawBody;
	}
}
