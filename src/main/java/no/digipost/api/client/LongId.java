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

abstract class LongId {

    private final long value;
    private final String stringValue;

    LongId(long value) {
        this.value = value;
        this.stringValue = String.valueOf(value);
    }

    public final long value() {
        return value;
    }

    public final String stringValue() {
        return stringValue;
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + ": " + stringValue();
    }

    @Override
    public final boolean equals(Object obj) {
        return getClass().isInstance(obj) && ((LongId) obj).value == this.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}
