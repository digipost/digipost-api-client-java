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
import no.digipost.api.client.representations.ErrorType;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static java.util.Arrays.asList;

public class DigipostClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;
	private final String errorMessage;

	public DigipostClientException(ErrorMessage error) {
		this(ErrorCode.resolve(error.getErrorCode()), getMessage(error), null);
	}

	public DigipostClientException(ErrorCode code, Throwable cause) {
		this(code, getMessage(cause), cause);
	}

	public DigipostClientException(ErrorCode code, String message) {
		this(code, message, null);
	}

	private DigipostClientException(ErrorCode code, String message, Throwable cause) {
		super(code + ": " + message, cause);
		this.errorCode = code;
		this.errorMessage = message;
	}

	public boolean isOneOf(ErrorCode ... codes) {
		return isOneOf(asList(codes));
	}

	public boolean isOneOf(Iterable<ErrorCode> codes) {
		for (ErrorCode code : codes) {
			if (code == errorCode) {
				return true;
			}
		}
		return false;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public ErrorType getErrorType() {
		return errorCode.getErrorType();
	}

	private static String getMessage(Throwable t) {
		Throwable rootCause = ExceptionUtils.getRootCause(t);
		return rootCause != null ? rootCause.getClass().getName() + ": " + rootCause.getMessage() : null;
	}

	private static String getMessage(ErrorMessage error) {
		return (ErrorCode.isKnown(error.getErrorCode()) ? "" : "(unknown errorcode: " + error.getErrorCode() + ") ") + error.getErrorMessage();
	}

}
