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

import static java.lang.String.format;

public class UnexpectedResponseException extends UserDocumentsApiException {

	public UnexpectedResponseException(final StatusLine status, final ErrorCode errorCode, final String errorMessage) {
		this(status, errorCode, errorMessage, null);
	}

	public UnexpectedResponseException(final StatusLine status, final ErrorCode errorCode, final String errorMessage, final Throwable cause) {
		super(errorCode, format("Unexpected response: status [%s - %s], error [%s - %s]",
				status.getStatusCode(),
				status.getReasonPhrase(),
				errorCode,
				errorMessage), cause);
	}

	public UnexpectedResponseException(final StatusLine status, final Error error) {
		this(status, error.getCode(), error.getMessage());
	}
}
