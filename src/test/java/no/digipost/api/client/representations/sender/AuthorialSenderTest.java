/*
 * Copyright (C) Posten Bring AS
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
import no.digipost.api.client.BrokerId;
import no.digipost.api.client.SenderId;
import no.digipost.api.client.representations.MayHaveSender;
import no.digipost.api.client.representations.SenderOrganization;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthorialSenderTest {

    @Test
    public void correctEqualsAndHashCode() {
        EqualsVerifier.forClass(AuthorialSender.class).verify();
    }

    @Test
    public void failsIfOrganizationIsQueriedFromAnIdBasedSender() {
        AuthorialSender sender = AuthorialSender.resolve(BrokerId.of(42), MayHaveSender.NO_SENDER);
        assertThat(sender.getAccountId(), is(SenderId.of(42L)));
        assertThrows(IllegalStateException.class, sender::getOrganization);
    }

    @Test
    public void failsIfIdIsQueriedFromAnOrganizationBasedSender() {
        SenderOrganization org = new SenderOrganization("10", null);
        AuthorialSender sender = AuthorialSender.resolve(BrokerId.of(42), new MayHaveSender() {
            @Override public Optional<SenderOrganization> getSenderOrganization() { return Optional.of(org); }
            @Override public Optional<SenderId> getSenderId() { return Optional.empty(); }
        });
        assertThat(sender.getOrganization(), is(org));
        assertThrows(IllegalStateException.class, sender::getAccountId);
    }
}
