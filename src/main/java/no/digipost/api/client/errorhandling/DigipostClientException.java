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
package no.digipost.api.client.errorhandling;

import no.digipost.api.client.representations.ErrorMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class DigipostClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ErrorType errorType;
	private final String errorMessage;

	public DigipostClientException(ErrorMessage error) {
		this(ErrorType.resolve(error.getErrorCode()), getMessage(error), null);
	}

	public DigipostClientException(ErrorType type, Throwable cause) {
		this(type, getMessage(cause), cause);
	}

	public DigipostClientException(ErrorType type, String message) {
		this(type, message, null);
	}

	private DigipostClientException(ErrorType type, String message, Throwable cause) {
		super(type + ": " + message, cause);
		this.errorType = type;
		this.errorMessage = message;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	private static String getMessage(Throwable t) {
		Throwable rootCause = ExceptionUtils.getRootCause(t);
		return rootCause != null ? rootCause.getClass().getName() + ": " + rootCause.getMessage() : null;
	}

	private static String getMessage(ErrorMessage error) {
		return (ErrorType.isKnown(error.getErrorCode()) ? "" : "(unknown errorcode: " + error.getErrorCode() + ") ") + error.getErrorMessage();
	}

}
