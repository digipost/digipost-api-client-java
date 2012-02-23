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
package no.digipost.api.client;

public class DigipostClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public enum ErrorType {
		RECIPIENT_DOES_NOT_EXIST,
		PROBLEM_WITH_REQUEST,
		SERVER_ERROR,
		INVALID_TRANSACTION,
		MESSAGE_DOES_NOT_EXIST,
		DUPLICATE_MESSAGE_ID,
		DIGIPOST_MESSAGE_ALREADY_DELIVERED,
		PRINT_MESSAGE_ALREADY_DELIVERED,
		GENERAL_ERROR,
		CANNOT_PREENCRYPT,
		FAILED_PREENCRYPTION,
		NOT_AUTHORIZED_FOR_PRINT;
	}

	private final ErrorType errorType;
	private final String errorMessage;

	public DigipostClientException(final ErrorType errorType, final String errorMessage) {
		super(errorType.name() + ": " + errorMessage);
		this.errorType = errorType;
		this.errorMessage = errorMessage;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
