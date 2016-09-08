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

public enum ErrorCode {
	DOCUMENT_NOT_FOUND,
	AGREEMENT_TYPE_NOT_AVAILABLE,
	UNKNOWN_USER_ID,
	NOT_AUTHORIZED,
	AGREEMENT_NOT_FOUND,
	AGREEMENT_DELETED,
	INVALID_INVOICE_STATUS,
	INVALID_REQUEST_PARAMETER,

	BROKER_NOT_AUTHORIZED,
	INVALID_BROKER_ID,
	INVALID_SENDER_ID,

	CLIENT_TECHNICAL_ERROR,
	SIGNATURE_ERROR,
	IO_EXCEPTION,
	GENERAL_ERROR;

	public static ErrorCode parse(final String error) {
		try {
			return valueOf(error.toUpperCase());
		} catch (IllegalArgumentException e) {
			return GENERAL_ERROR;
		}
	}
}
