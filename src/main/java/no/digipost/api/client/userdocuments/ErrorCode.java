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

public class ErrorCode extends JustA<String> implements IsEqual<ErrorCode> {

	public static final ErrorCode DOCUMENT_NOT_FOUND = new ErrorCode("DOCUMENT_NOT_FOUND");
	public static final ErrorCode AGREEMENT_TYPE_NOT_AVAILABLE = new ErrorCode("AGREEMENT_TYPE_NOT_AVAILABLE");
	public static final ErrorCode UNKNOWN_USER_ID = new ErrorCode("UNKNOWN_USER_ID");
	public static final ErrorCode NOT_AUTHORIZED = new ErrorCode("NOT_AUTHORIZED");
	public static final ErrorCode AGREEMENT_NOT_FOUND = new ErrorCode("AGREEMENT_NOT_FOUND");
	public static final ErrorCode AGREEMENT_DELETED = new ErrorCode("AGREEMENT_DELETED");
	public static final ErrorCode CLIENT_TECHNICAL_ERROR = new ErrorCode("CLIENT_TECHNICAL_ERROR");
	public static final ErrorCode SIGNATURE_ERROR = new ErrorCode("SIGNATURE_ERROR");
	public static final ErrorCode GENERAL_ERROR = new ErrorCode("GENERAL_ERROR");
	public static final ErrorCode IO_EXCEPTION = new ErrorCode("IO_EXCEPTION_ERROR");
	public static final ErrorCode SERVER_SIGNATURE_ERROR = new ErrorCode("SERVER_SIGNATURE_ERROR");

	public ErrorCode(final String errorCode) {
		super(errorCode.toUpperCase());
	}

	@Override
	public String toString() {
		return serialize();
	}

	@Override
	public String serialize() {
		return value;
	}

	@Override
	public boolean isEqual(final ErrorCode that) {
		return this.equals(that);
	}
}
