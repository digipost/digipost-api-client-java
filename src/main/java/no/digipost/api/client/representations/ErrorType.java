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
package no.digipost.api.client.representations;

import org.apache.http.StatusLine;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "error-type")
@XmlEnum
public enum ErrorType {

    SERVER,
    CLIENT_DATA,
    CLIENT_TECHNICAL,
    CONFIGURATION,
    NONE;

    public String value() {
        return name();
    }

    public static ErrorType fromValue(final String v) {
        return valueOf(v);
    }

    public static ErrorType fromResponseStatus(StatusLine serverResponseStatus) {
        int statusCode = serverResponseStatus.getStatusCode();
        if (statusCode >= 400 && statusCode < 500) {
            return ErrorType.CLIENT_TECHNICAL;
        } else if (statusCode >= 500) {
            return ErrorType.SERVER;
        } else {
            return NONE;
        }
    }

}
