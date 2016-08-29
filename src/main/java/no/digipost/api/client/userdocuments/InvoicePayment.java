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

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "invoice-payment")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoicePayment {

	@XmlElement(name = "payment-id")
	private int paymentId;
	@XmlElement(name = "paid-at", required = true, type = String.class)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	private DateTime paidAt;
	@XmlElement(name = "from-account")
	private String fromAccount;

	public InvoicePayment() {
	}

	public InvoicePayment(final int paymentId, final DateTime paidAt, final BankAccountNumber fromAccount) {
		this.paymentId = paymentId;
		this.paidAt = paidAt;
		this.fromAccount = fromAccount.getAccountNumber();
	}

	public int getPaymentId() {
		return paymentId;
	}

	public DateTime getPaidAt() {
		return paidAt;
	}

	public BankAccountNumber getFromAccount() {
		return new BankAccountNumber(fromAccount);
	}
}
