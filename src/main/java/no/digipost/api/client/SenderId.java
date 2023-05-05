/*
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
package no.digipost.api.client;

public final class SenderId extends LongId {

    public static SenderId of(long id) {
        return new SenderId(id);
    }

    private SenderId(long id) {
        super(id);
    }

    /**
     * Convert this ID to a broker ID.
     *
     * @return this ID as a broker ID
     */
    public BrokerId asBrokerId() {
        return BrokerId.of(this.value());
    }

}
