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
 /*
 * An implementation of the JustA-pattern from github.com/digipost/digg,
 * with added support for validation, that will also work with JDK7
 *
 */
package no.digipost.api.client.userdocuments;

public abstract class JustAValid<T> extends JustA<T> {

    protected JustAValid(T value, String message) {
    	super(value);
        if (!isValid(value)) {
            throw new IllegalArgumentException("Invalid value " + value + " for " + getClass().getName() + " : " + message);
        }
    }

    public abstract boolean isValid(T value);
}
