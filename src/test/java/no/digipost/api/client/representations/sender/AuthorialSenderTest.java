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

import nl.jqno.equalsverifier.EqualsVerifier;
import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.SenderOrganization;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AuthorialSenderTest {

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Test
	public void correctEqualsAndHashCode() {
		EqualsVerifier.forClass(AuthorialSender.class).verify();
	}

	@Test
	public void failsIfOrganizationIsQueriedFromAnIdBasedSender() {
		AuthorialSender sender = AuthorialSender.resolve(42, MayHaveSender.NO_SENDER);
		assertThat(sender.getAccountId(), is(42L));
		expectedException.expect(IllegalStateException.class);
		sender.getOrganization();
	}

	@Test
	public void failsIfIdIsQueriedFromAnOrganizationBasedSender() {
		final SenderOrganization org = new SenderOrganization("10", null);
		AuthorialSender sender = AuthorialSender.resolve(42, new MayHaveSender() {
			@Override public SenderOrganization getSenderOrganization() { return org; }
			@Override public Long getSenderId() { return null; }
		});
		assertThat(sender.getOrganization(), is(org));
		expectedException.expect(IllegalStateException.class);
		sender.getAccountId();
	}
}
