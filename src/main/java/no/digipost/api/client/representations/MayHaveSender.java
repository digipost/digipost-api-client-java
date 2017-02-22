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


/**
 * A type which may or may not have specified a sender, either
 * as a {@link #getSenderId() sender ID} or an
 * {@link #getSenderOrganization() organization}. Both these methods
 * may return {@code null}.
 */
public interface MayHaveSender {

    public static final MayHaveSender NO_SENDER = new MayHaveSender() {
        @Override public SenderOrganization getSenderOrganization() { return null; }
        @Override public Long getSenderId() { return null; }
    };

    /**
     * @return the sender ID. May be {@code null}.
     */
    Long getSenderId();

    /**
     * @return the sender organization. May be {@code null}.
     */
    SenderOrganization getSenderOrganization();

}
