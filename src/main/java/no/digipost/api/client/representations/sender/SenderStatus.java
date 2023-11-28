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
package no.digipost.api.client.representations.sender;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Status for en avsender.
 *
 * @see #VALID_SENDER
 * @see #NO_INFO_AVAILABLE
 */
@XmlType(name = "sender-status")
@XmlEnum
public enum SenderStatus {

    /**
     * No information about the requested sender could be retrieved. This may either be
     * because the sender does not exist, or the broker is not authorized to send on
     * behalf of the sender.
     */
    NO_INFO_AVAILABLE,

    /**
     * The sender exists in Digipost, and the broker is authorized to act on behalf
     * of the sender (typically send messages).
     */
    VALID_SENDER;

}
