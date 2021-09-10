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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "event-type")
@XmlEnum
public enum DocumentEventType {

    EMAIL_MESSAGE_FAILED,
    EMAIL_MESSAGE_SENT,
    EMAIL_NOTIFICATION_FAILED,
    SMS_NOTIFICATION_FAILED,
    OPENED,
    MOVE_FILES_FROM_PUBLIC_SECTOR,
    POSTMARKED,
    PRINT_FAILED,
    PEPPOL_FAILED,
    PEPPOL_DELIVERED,
    SHREDDED;

}
