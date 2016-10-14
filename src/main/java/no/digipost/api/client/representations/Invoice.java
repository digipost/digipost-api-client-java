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
package no.digipost.api.client.representations;

import no.digipost.api.client.representations.xml.DateXmlAdapter;
import org.joda.time.LocalDate;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.math.BigDecimal;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "invoice", propOrder = {
    "kid",
    "amount",
    "account",
    "dueDate"
})
public class Invoice
    extends Document
{

    @XmlElement(required = true)
    protected String kid;
    @XmlElement(required = true)
    protected BigDecimal amount;
    @XmlElement(required = true)
    protected String account;
    @XmlElement(name = "due-date", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate dueDate;

	Invoice() {
	}

	/**
	 * Constructor for just the required fields of a document. Non-specified
	 * fields will get their respective standard values when sent to Digipost.
	 */
	public Invoice(String uuid, String subject, FileType fileType, String kid, BigDecimal amount, String account, LocalDate dueDate) {
		this(uuid, subject, fileType, null, null, null, null, null, kid, amount, account, dueDate);
	}

	public Invoice(String uuid, String subject, FileType fileType, String kid, BigDecimal amount, String account, LocalDate dueDate,
				   Boolean opened, String technicalType, AuthenticationLevel authenticationLevel) {
		this(uuid, subject, fileType, null, null, null, authenticationLevel, null, kid, amount, account, dueDate, opened, technicalType);
	}

	public Invoice(String uuid, String subject, FileType fileType, String openingReceipt, SmsNotification smsNotification,
	               EmailNotification emailNotification, AuthenticationLevel authenticationLevel, SensitivityLevel sensitivityLevel,
				   String kid, BigDecimal amount, String account, LocalDate dueDate) {
		this(uuid, subject, fileType, openingReceipt, smsNotification, emailNotification, authenticationLevel, sensitivityLevel,
				kid, amount, account, dueDate, null, null);
	}

	public Invoice(String uuid, String subject, FileType fileType, String openingReceipt, SmsNotification smsNotification,
				   EmailNotification emailNotification, AuthenticationLevel authenticationLevel, SensitivityLevel sensitivityLevel,
				   String kid, BigDecimal amount, String account, LocalDate dueDate, Boolean opened, String... technicalType) {
		super(uuid, subject, fileType, openingReceipt, smsNotification, emailNotification, authenticationLevel, sensitivityLevel,
				opened, technicalType);
		this.kid = kid;
		this.amount = amount;
		this.account = account;
		this.dueDate = dueDate;
	}

}
