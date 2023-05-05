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

import org.slf4j.Logger;

public interface EventLogger {
    EventLogger NOOP_LOGGER = eventText -> {};

    void log(String logMessage);

    default EventLogger withDebugLogTo(Logger slf4jLogger) {
        EventLogger nested = EventLogger.this;
        return logMessage -> {
            slf4jLogger.debug(logMessage);
            nested.log(logMessage);
        };
    }
}
