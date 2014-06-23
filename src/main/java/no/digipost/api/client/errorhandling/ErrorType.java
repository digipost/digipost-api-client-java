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

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

public enum ErrorType {

	GENERAL_ERROR,
	PROBLEM_WITH_REQUEST,
	SERVER_ERROR,

	// client errors
	INVALID_TRANSACTION,
	SERVER_SIGNATURE_ERROR,
	MESSAGE_DOES_NOT_EXIST,
	DUPLICATE_MESSAGE_ID,
	CANNOT_PREENCRYPT,
	FAILED_PREENCRYPTION,
	DIGIPOST_MESSAGE_ALREADY_DELIVERED,
	PRINT_MESSAGE_ALREADY_DELIVERED,
	CONNECTION_ERROR(ConnectException.class),

	// Configuration (error from server)
	NOT_APPROVED_FOR_REST_API_USAGE,
	NOT_APPROVED_FOR_PRINT,
	NOT_APPROVED_FOR_IDPORTEN,
	NOT_APPROVED_FOR_DIRECT_PRINT,
	NOT_APPROVED_FOR_PREENCRYPT,
	BROKER_NOT_AUTHORIZED,
	UNKNOWN_USER_ID,
	MISSING_CERTIFICATE,
	REVOKED_CERTIFICATE,

	// Input (error from server)
	MESSAGE_NOT_FOUND,
	DOCUMENT_NOT_FOUND,
	MESSAGE_ALREADY_SENT,
	REQUEST_TOO_LARGE,
	ORGANISATION_LETTERS_PER_MONTH_EXCEEDED,
	CONTENT_OF_PRINT_MESSAGE_MUST_BE_PDF,
	DUPLICATE_MESSAGE,
	DUPLICATE_DOCUMENT_ID,
	NOTIFICATION_ADDRESSES_NOT_ALLOWED,
	UNKNOWN_SENDER,
	UNKNOWN_RECIPIENT,
	MISSING_RECIPIENT,
	MISSING_SUBJECT,
	INVALID_EMAIL_ADDRESS,
	INVALID_EMAIL_NOTIFICATION_TIME,
	INVALID_SMS_NOTIFICATION_TIME,
	INVALID_PHONE_NUMBER,
	INVALID_RECIPIENT_PRINT_ADDRESS,
	INVALID_PDF_CONTENT,
	INVALID_MONETARY_AMOUNT,
	AUTHENTICATION_LEVEL_TO_LOW,
	ILLEGAL_CONTENT_TYPE,
	BAD_CONTENT,
	ILLEGAL_HTML_CONTENT,
	FILE_TOO_LARGE,
	CONTENT_ALREADY_UPLOADED;


	private static final Map<String, ErrorType> errorByName = new HashMap<>(); static {
		for (ErrorType errorType : values()) {
	        errorByName.put(errorType.name(), errorType);
        }
	}

	private final List<Class<? extends Throwable>> fittingThrowables;


	@SafeVarargs
    ErrorType(Class<? extends Throwable> ... fittingThrowables) {
		this.fittingThrowables = asList(fittingThrowables);
	}


	/**
	 * @return <code>ErrorType</code> with the same name as the given <code>errorCode</code>, or falls
	 *         back to {@link ErrorType#GENERAL_ERROR GENERAL_ERROR} if no such <code>ErrorType</code>
	 *         is found.
	 */
	public static ErrorType resolve(String errorCode) {
		ErrorType resolved = errorByName.get(errorCode);
		return resolved != null ? resolved : GENERAL_ERROR;
	}

	public static boolean isKnown(String errorCode) {
		return errorByName.containsKey(errorCode);
	}



	/**
	 * @return An ErrorType that fits with the root cause of the given {@link Throwable}. If
	 *         the root cause is unknown, {@link ErrorType#GENERAL_ERROR GENERAL_ERROR} is
	 *         returned.
	 */
	public static ErrorType resolve(Throwable t) {
		Throwable rootCause = getRootCause(t);
		Class<? extends Throwable> throwableType = (rootCause != null ? rootCause : t).getClass();
		for (ErrorType error : values()) {
			if (error.fittingThrowables.contains(throwableType)) {
				return error;
			}
        }
		return GENERAL_ERROR;
	}
}