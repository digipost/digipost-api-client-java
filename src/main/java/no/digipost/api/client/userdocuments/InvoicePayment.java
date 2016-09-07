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

public class InvoicePayment {

	private Integer paymentId;
	private BankAccountNumber fromAccount;

	public InvoicePayment(final Integer paymentId, final BankAccountNumber fromAccount) {
		this.paymentId = paymentId;
		this.fromAccount = fromAccount;
	}

	public int getPaymentId() {
		return paymentId;
	}

	public BankAccountNumber getFromAccount() {
		return fromAccount;
	}

	public InvoiceUpdate asInvoiceUpdate() {
		return new InvoiceUpdate(InvoiceStatus.PAID, paymentId, fromAccount);
	}
}
