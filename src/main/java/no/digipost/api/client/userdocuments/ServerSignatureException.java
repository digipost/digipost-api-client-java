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

import no.digipost.api.client.util.ResponseExceptionSupplier;
import org.apache.http.StatusLine;

public class ServerSignatureException extends UnexpectedResponseException {
	public ServerSignatureException(final StatusLine status, final String errorMessage) {
		this(status, errorMessage, null);
	}

	public ServerSignatureException(final StatusLine status, final String errorMessage, final Throwable cause) {
		super(status, ErrorCode.SIGNATURE_ERROR, errorMessage, cause);
	}

	public static ResponseExceptionSupplier<ServerSignatureException> getExceptionSupplier() {
		return new ResponseExceptionSupplier<ServerSignatureException>() {
			@Override
			public ServerSignatureException get(final StatusLine status, final String message) {
				return new ServerSignatureException(status, message);
			}
		};
	}
}
