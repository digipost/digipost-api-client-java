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

import no.digipost.api.client.representations.ErrorType;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.ErrorType.*;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

public enum ErrorCode {

	// Server errors
	GENERAL_ERROR(SERVER),
	API_UNAVAILABLE(SERVER),
	SERVER_ERROR(SERVER),

	SERVER_SIGNATURE_ERROR(SERVER),

	// Configuration errors
	NOT_APPROVED_FOR_REST_API_USAGE(CONFIGURATION),
	NOT_APPROVED_FOR_PRINT(CONFIGURATION),
	NOT_APPROVED_FOR_IDPORTEN(CONFIGURATION),
	NOT_APPROVED_FOR_DIRECT_PRINT(CONFIGURATION),
	NOT_APPROVED_FOR_PREENCRYPT(CONFIGURATION),
	MISSING_CERTIFICATE(CONFIGURATION),
	REVOKED_CERTIFICATE(CONFIGURATION),
	BROKER_NOT_AUTHORIZED(CONFIGURATION),
	NOTIFICATION_ADDRESSES_NOT_ALLOWED(CONFIGURATION),
	ORGANISATION_LETTERS_PER_MONTH_EXCEEDED(CONFIGURATION),

	// Client technical
	CANNOT_PREENCRYPT(CLIENT_TECHNICAL),
	FAILED_PREENCRYPTION(CLIENT_TECHNICAL),
	PROBLEM_WITH_REQUEST(CLIENT_TECHNICAL),
	INVALID_TRANSACTION(CLIENT_TECHNICAL),
	DIGIPOST_MESSAGE_ALREADY_DELIVERED(CLIENT_TECHNICAL),
	PRINT_MESSAGE_ALREADY_DELIVERED(CLIENT_TECHNICAL),
	CONNECTION_ERROR(CLIENT_TECHNICAL, ConnectException.class),

	SCHEMA_VALIDATION_ERROR(CLIENT_TECHNICAL),
	ILLEGAL_ACCESS(CLIENT_TECHNICAL),
	INVALID_SIGNATURE(CLIENT_TECHNICAL),
	MISSING_SIGNATURE(CLIENT_TECHNICAL),
	CONTENT_NOT_ENCRYPTED_WITH_CORRECT_KEY(CLIENT_TECHNICAL),
	ENCRYPTION_KEY_NOT_FOUND(CLIENT_TECHNICAL),
	CONTENT_NOT_ENCRYPTED(CLIENT_TECHNICAL),
	MESSAGE_ALREADY_SENT(CLIENT_TECHNICAL),
	CONTENT_ALREADY_UPLOADED(CLIENT_TECHNICAL),
	MESSAGE_NOT_FOUND(CLIENT_TECHNICAL),
	DOCUMENT_NOT_FOUND(CLIENT_TECHNICAL),
	INVALID_USER_ID_HEADER(CLIENT_TECHNICAL),
	UNKNOWN_USER_ID(CLIENT_TECHNICAL),
	MISSING_DATE_HEADER(CLIENT_TECHNICAL),
	INVALID_DATE_HEADER(CLIENT_TECHNICAL),
	DATE_HEADER_OUTSIDE_ACCEPTED_INTERVAL(CLIENT_TECHNICAL),
	MISSING_SHA256(CLIENT_TECHNICAL),
	INVALID_SHA256(CLIENT_TECHNICAL),
	MISSING_CONTENT_HASH(CLIENT_TECHNICAL),
	INVALID_MD5(CLIENT_TECHNICAL),
	MISSING_BODYPART_CONTENT_DISPOSITION(CLIENT_TECHNICAL),
	MISSING_BODYPART_FILENAME(CLIENT_TECHNICAL),
	DOCUMENTS_AND_FILES_MISMATCH(CLIENT_TECHNICAL),

	// Client data
	REQUEST_TOO_LARGE(CLIENT_DATA),
	CONTENT_OF_PRINT_MESSAGE_MUST_BE_PDF(CLIENT_DATA),
	DUPLICATE_MESSAGE(CLIENT_DATA),
	DUPLICATE_DOCUMENT_ID(CLIENT_DATA),
	FILE_TOO_LARGE(CLIENT_DATA),
	ILLEGAL_HTML_CONTENT(CLIENT_DATA),
	BAD_CONTENT(CLIENT_DATA),
	ILLEGAL_CONTENT_TYPE(CLIENT_DATA),
	VALIDATION_FAILED(CLIENT_DATA),
	UNKNOWN_SENDER(CLIENT_DATA),
	UNKNOWN_RECIPIENT(CLIENT_DATA),
	MISSING_RECIPIENT(CLIENT_DATA),
	MISSING_CONTENT(CLIENT_DATA),
	MISSING_SUBJECT(CLIENT_DATA),
	INVALID_EMAIL_ADDRESS(CLIENT_DATA),
	INVALID_EMAIL_NOTIFICATION_TIME(CLIENT_DATA),
	INVALID_SMS_NOTIFICATION_TIME(CLIENT_DATA),
	INVALID_PHONE_NUMBER(CLIENT_DATA),
	INVALID_RECIPIENT_PRINT_ADDRESS(CLIENT_DATA),
	INVALID_PDF_CONTENT(CLIENT_DATA),
	INVALID_MONETARY_AMOUNT(CLIENT_DATA),
	AUTHENTICATION_LEVEL_TOO_LOW(CLIENT_DATA)
	;

	private static final Map<String, ErrorCode> errorByName = new HashMap<>(); static {
		for (ErrorCode errorCode : values()) {
	        errorByName.put(errorCode.name(), errorCode);
        }
	}

	private final ErrorType errorType;
	private final List<Class<? extends Throwable>> fittingThrowables;

	@SafeVarargs
	ErrorCode(ErrorType errorType, Class<? extends Throwable>... fittingThrowables) {
		this.errorType = errorType;
		this.fittingThrowables = asList(fittingThrowables);
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	/**
	 * @return <code>ErrorCode</code> with the same name as the given <code>errorCode</code>, or falls
	 *         back to {@link ErrorCode#GENERAL_ERROR GENERAL_ERROR} if no such <code>ErrorCode</code>
	 *         is found.
	 */
	public static ErrorCode resolve(String errorCode) {
		ErrorCode resolved = errorByName.get(errorCode);
		return resolved != null ? resolved : GENERAL_ERROR;
	}

	public static boolean isKnown(String errorCode) {
		return errorByName.containsKey(errorCode);
	}



	/**
	 * @return An ErrorCode that fits with the root cause of the given {@link Throwable}. If
	 *         the root cause is unknown, {@link ErrorCode#GENERAL_ERROR GENERAL_ERROR} is
	 *         returned.
	 */
	public static ErrorCode resolve(Throwable t) {
		Throwable rootCause = getRootCause(t);
		Class<? extends Throwable> throwableType = (rootCause != null ? rootCause : t).getClass();
		for (ErrorCode error : values()) {
			if (error.fittingThrowables.contains(throwableType)) {
				return error;
			}
        }
		return GENERAL_ERROR;
	}
}