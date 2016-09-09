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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "invoice-update")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceUpdate {

	@XmlElement
	private String status;
	@XmlElement(name = "payment-id")
	private Integer paymentId;
	@XmlElement(name = "from-account")
	private String fromAccount;

	public InvoiceUpdate() {
	}

	public InvoiceUpdate(final InvoiceStatus status) {
		this(status, null, null);
	}

	public InvoiceUpdate(final InvoiceStatus status, final Integer paymentId, final BankAccountNumber fromAccount) {
		this.status = status.getStatus();
		this.paymentId = paymentId;
		this.fromAccount = fromAccount != null ? fromAccount.getAccountNumber() : null;
	}

	public InvoiceStatus getStatus () {
		return InvoiceStatus.valueOf(status);
	}

	public int getPaymentId() {
		return paymentId;
	}

	public BankAccountNumber getFromAccount() {
		return new BankAccountNumber(fromAccount);
	}
}