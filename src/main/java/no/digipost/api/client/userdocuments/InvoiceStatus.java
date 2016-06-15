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

import java.util.Objects;

public class InvoiceStatus {

	public static final InvoiceStatus UNPAID = new InvoiceStatus("unpaid");
	public static final InvoiceStatus PAID = new InvoiceStatus("paid");
	public static final InvoiceStatus DELETED = new InvoiceStatus("deleted");

	private final String status;

	public InvoiceStatus(final String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final InvoiceStatus that = (InvoiceStatus) o;
		return Objects.equals(status, that.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(status);
	}
}
