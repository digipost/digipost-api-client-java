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


import no.digipost.api.client.representations.ErrorMessage;

import java.util.Objects;

public class Error {

	public static final Error DOCUMENT_NOT_FOUND = new Error("DOCUMENT_NOT_FOUND", "Document not found");
	public static final Error AGREEMENT_TYPE_NOT_AVAILABLE = new Error("AGREEMENT_TYPE_NOT_AVAILABLE", "Sepcified agreement type is not available");
	public static final Error UNKNOWN_USER_ID = new Error("UNKNOWN_USER_ID", "The user-id is not a Digipost user");
	public static final Error NOT_AUTHORIZED = new Error("NOT_AUTHORIZED", "Not authorized");
	public static final Error AGREEMENT_NOT_FOUND = new Error("AGREEMENT_NOT_FOUND", "Agreement not fount");
	public static final Error AGREEMENT_DELETED = new Error("AGREEMENT_DELETED", "Agreement has bee deleted by the user");

	private final String code;
	private final String message;

	private Error(final String code, final String message) {
		this.code = code;
		this.message = message;
	}

	public Error withCustomMessage(String message) {
		return new Error(code, message);
	}

	public static Error fromErrorMessage(final ErrorMessage errorMessage) {
		return new Error(errorMessage.getErrorCode(), errorMessage.getErrorMessage());
	}

	public boolean is(final Error other) {
		return this.code.equalsIgnoreCase(other.code);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Error{");
		sb.append("code='").append(code).append('\'');
		sb.append(", message='").append(message).append('\'');
		sb.append('}');
		return sb.toString();
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return is((Error) o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}
}
