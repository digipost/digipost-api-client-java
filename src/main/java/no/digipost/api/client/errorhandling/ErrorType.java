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
package no.digipost.api.client.errorhandling;

import java.util.HashMap;
import java.util.Map;

public enum ErrorType {
    SERVER,
    CONFIGURATION,
    CLIENT_TECHNICAL,
    CLIENT_DATA,
    NONE,
    UNKNOWN;

    public static final Map<no.digipost.api.client.representations.ErrorType, ErrorType> toErrorTypeMap = new HashMap<>();

    static {
        toErrorTypeMap.put(no.digipost.api.client.representations.ErrorType.SERVER, SERVER);
        toErrorTypeMap.put(no.digipost.api.client.representations.ErrorType.CLIENT_DATA, CLIENT_DATA);
        toErrorTypeMap.put(no.digipost.api.client.representations.ErrorType.CLIENT_TECHNICAL, CLIENT_TECHNICAL);
        toErrorTypeMap.put(no.digipost.api.client.representations.ErrorType.CONFIGURATION, CONFIGURATION);
        toErrorTypeMap.put(no.digipost.api.client.representations.ErrorType.NONE, NONE);
    }

    public static ErrorType resolve(no.digipost.api.client.representations.ErrorType errorType) {
        ErrorType translated = toErrorTypeMap.get(errorType);
        return translated != null ? translated : UNKNOWN;
    }
}
