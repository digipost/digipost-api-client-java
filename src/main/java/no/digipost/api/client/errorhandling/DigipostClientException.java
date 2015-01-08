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

import static java.util.Arrays.asList;
import static no.digipost.api.client.errorhandling.ErrorType.*;

public class DigipostClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;
	private final ErrorType errorType;
	private final String errorMessage;

	public DigipostClientException(ErrorMessage error) {
		this(ErrorCode.resolve(error.getErrorCode()), resolve(error.getErrorType()), getMessage(error), null);
	}

	public DigipostClientException(ErrorCode code, Throwable cause) {
		this(code, NONE, getMessage(cause), cause);
	}

	public DigipostClientException(ErrorCode code, String message) {
		this(code, NONE, message, null);
	}

	public DigipostClientException(ErrorCode code, String message, Throwable cause) {
		this(code, NONE, message, cause);
	}

	private DigipostClientException(ErrorCode code, ErrorType errorTypeFromServer, String message, Throwable cause) {
		super(code + ": " + message, cause);
		this.errorCode = code;
		this.errorType = code.getOverriddenErrorType() == UNKNOWN ? errorTypeFromServer : code.getOverriddenErrorType();
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
		return errorType;
	}

	private static String getMessage(Throwable t) {
		Throwable rootCause = ExceptionUtils.getRootCause(t);
		return rootCause != null ? rootCause.getClass().getName() + ": " + rootCause.getMessage() : null;
	}

	private static String getMessage(ErrorMessage error) {
		String prefix = "";
		if (!ErrorCode.isKnown(error.getErrorCode())) {
			prefix = String.format("(unknown errorcode: %s with errorType: %s) ", error.getErrorCode(), error.getErrorType().name());
		}
		return prefix + error.getErrorMessage();
	}

}
