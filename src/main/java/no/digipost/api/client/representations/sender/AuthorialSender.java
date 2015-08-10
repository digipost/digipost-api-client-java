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
package no.digipost.api.client.representations.sender;

import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.SenderOrganization;

import java.util.Objects;

/**
 * The <em>authorial sender</em> of a message, i.e. what the receiver sees
 * as the sender.
 *
 * It is resolved based on the broker ID, and the optional presence of either a
 * {@link MayHaveSender#getSenderId() sender-ID} or
 * {@link MayHaveSender#getSenderOrganization() sender organization}. If neither of
 * those are present, the broker ID is used as the authorial sender.
 *
 *
 */
public final class AuthorialSender {

	public enum Type {
		ACCOUNT_ID, ORGANIZATION;
	}

	public static AuthorialSender resolve(long brokerId, MayHaveSender mayHaveAuthorialSender) {
		if (mayHaveAuthorialSender.getSenderId() != null) {
			return new AuthorialSender(mayHaveAuthorialSender.getSenderId(), null);
		} else if (mayHaveAuthorialSender.getSenderOrganization() != null) {
			return new AuthorialSender(null, mayHaveAuthorialSender.getSenderOrganization());
		} else {
			return new AuthorialSender(brokerId, null);
		}
	}



	private final Long id;
	private final SenderOrganization organization;

	private AuthorialSender(Long id, SenderOrganization organization) {
		this.id = id;
		this.organization = organization;
	}

	public boolean is(Type type) {
		switch (type) {
    		case ACCOUNT_ID: return id != null;
    		case ORGANIZATION: return organization != null;
    		default: return false;
		}
	}

	public long getAccountId() {
		if (!is(Type.ACCOUNT_ID)) throw new IllegalStateException("account id of " + AuthorialSender.class.getSimpleName() + " is null. Actual: " + this);
		return id;
	}

	public SenderOrganization getOrganization() {
		if (!is(Type.ORGANIZATION)) throw new IllegalStateException("organization of " + AuthorialSender.class.getSimpleName() + " is null. Actual: " + this);
		return organization;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AuthorialSender) {
			AuthorialSender other = (AuthorialSender) obj;
			if (this.is(Type.ACCOUNT_ID)) return Objects.equals(this.id, other.id);
			if (this.is(Type.ORGANIZATION)) return Objects.equals(this.organization, other.organization);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (this.is(Type.ACCOUNT_ID)) return Objects.hashCode(id);
		if (this.is(Type.ORGANIZATION)) return Objects.hashCode(organization);
		else return super.hashCode();
	}


	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("authorial sender ");
		if (is(Type.ACCOUNT_ID)) {
			s.append(" ID: ").append(id);
		} else if (is(Type.ORGANIZATION)) {
			s.append(" organization: ").append(organization);
		} else {
			s.append(" <undefined>");
		}
		return s.toString();
	}

}
