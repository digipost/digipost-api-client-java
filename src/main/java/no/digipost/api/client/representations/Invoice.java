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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import no.digipost.api.client.representations.xml.DateXmlAdapter;
import org.joda.time.LocalDate;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "invoice", propOrder = {
    "kid",
    "amount",
    "account",
    "dueDate"
})
@XmlRootElement(name = "invoice")
public class Invoice
    extends Message
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

	public Invoice() {
	}

	public Invoice(final String messageId, final String subject, final PersonalIdentificationNumber id, final SmsNotification smsVarsling,
				   final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, final FileType fileType, final String kid, final BigDecimal amount,
				   final String account, final LocalDate dueDate) {
		super(messageId, subject, id, smsVarsling, authenticationLevel, sensitivityLevel, fileType);
		this.kid = kid;
		this.amount = amount;
		this.account = account;
		this.dueDate = dueDate;
	}

//	public Invoice(final String messageId, final String subject, final DigipostAddress digipostAdress, final SmsNotification smsVarsling,
//				   final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, final String kid, final BigDecimal amount,
//				   final String account, final LocalDate dueDate) {
//		super(messageId, subject, digipostAdress, smsVarsling, authenticationLevel, sensitivityLevel);
//		this.kid = kid;
//		this.amount = amount;
//		this.account = account;
//		this.dueDate = dueDate;
//	}

	public Invoice(final String messageId, final String subject, final RecipientIdentification recipient, final SmsNotification smsVarsling,
				   final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, FileType fileType, final String kid, final BigDecimal amount,
				   final String account, final LocalDate dueDate) {
		super(messageId, subject, recipient, smsVarsling, authenticationLevel, sensitivityLevel, fileType);
		this.kid = kid;
		this.amount = amount;
		this.account = account;
		this.dueDate = dueDate;
	}
}
