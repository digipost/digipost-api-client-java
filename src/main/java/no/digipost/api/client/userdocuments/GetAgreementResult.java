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

public class GetAgreementResult {

	public enum FailedReason {
		UNKNOWN_USER, NO_AGREEMENT, AGREEMENT_DELETED
	}

	private final Result<Agreement, FailedReason> result;

	public GetAgreementResult(final Agreement agreement) {
		this.result = new Result.Success<>(agreement);
	}

	public GetAgreementResult(final FailedReason failedReason) {
		this.result = new Result.Failure<>(failedReason);
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
}
