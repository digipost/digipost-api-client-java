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

import no.digipost.api.client.SenderId;

import java.util.Optional;

/**
 * A type which may or may not have specified a sender, either
 * as a {@link #getSenderId() sender ID} or an
 * {@link #getSenderOrganization() organization}. Both these methods
 * may return {@code null}.
 */
public interface MayHaveSender {

    public static final MayHaveSender NO_SENDER = new MayHaveSender() {
        @Override public Optional<SenderOrganization> getSenderOrganization() { return Optional.empty(); }
        @Override public Optional<SenderId> getSenderId() { return Optional.empty(); }
    };

    /**
     * @return the sender ID, or {@link Optional#empty()} if it does not exist.
     */
    Optional<SenderId> getSenderId();

    /**
     * @return the sender organization, or {@link Optional#empty()} if it does not exist.
     */
    Optional<SenderOrganization> getSenderOrganization();

}
