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

import no.digipost.api.client.util.Supplier;

public class GetAgreementResult {

	public enum FailedReason {
		UNKNOWN_USER, NO_AGREEMENT
	}

	private final Result<Agreement, FailedReason> result;

	public GetAgreementResult(final Agreement agreement) {
		this.result = new Result.Success<>(agreement);
	}

	public GetAgreementResult(final FailedReason failedReason, final Supplier<UnexpectedResponseException> agreementMissingExceptionSupplier) {
		this.result = new Result.Failure<>(failedReason, agreementMissingExceptionSupplier);
	}

	public boolean isSuccess() {
		return result.isSuccess();
	}

	public Agreement getAgreement() {
		return result.getValue();
	}

	public FailedReason getFailedReason() {
		return result.getError();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GetAgreementResult{");
		sb.append("result=").append(result);
		sb.append('}');
		return sb.toString();
	}
}
