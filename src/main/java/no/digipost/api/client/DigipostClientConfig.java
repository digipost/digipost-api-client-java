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

import java.net.URI;
import java.time.Clock;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

public final class DigipostClientConfig {

    public static Builder newConfiguration() {
        return new Builder();
    }

    public static class Builder {
        private Duration printKeyCacheTimeToLive = Duration.ofMinutes(5);
        private URI digipostApiUri = URI.create("https://api.digipost.no");
        private EventLogger eventLogger = EventLogger.NOOP_LOGGER;
        private Clock clock = Clock.systemDefaultZone();
        private boolean failOnHtmlDiff = false;

        private Builder() {
        }

        public Builder digipostApiUri(URI uri) {
            this.digipostApiUri = uri;
            return this;
        }

        public Builder disablePrintKeyCache() {
            return printKeyCacheTimeToLive(Duration.ZERO);
        }

        public Builder printKeyCacheTimeToLive(Duration cachePrintKey) {
            this.printKeyCacheTimeToLive = cachePrintKey;
            return this;
        }
        
        public Builder failOnHtmlSanitationDiff(){
            this.failOnHtmlDiff = true;
            return this;
        }

        public Builder eventLogger(EventLogger eventLogger) {
            this.eventLogger = eventLogger;
            return this;
        }

        public Builder clock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public DigipostClientConfig build() {
            return new DigipostClientConfig(digipostApiUri, printKeyCacheTimeToLive, eventLogger, clock, failOnHtmlDiff);
        }
    }





    public final URI digipostApiUri;
    public final Duration printKeyCacheTimeToLive;
    public final EventLogger eventLogger;
    public final Clock clock;
    public final boolean failOnHtmlDiff;

    private DigipostClientConfig(URI digipostApiUri, Duration printKeyCacheTimeToLive, EventLogger eventLogger, Clock clock, boolean failOnHtmlDiff) {
        this.digipostApiUri = requireNonNull(digipostApiUri, "digipostApiUri cat not be null");
        this.printKeyCacheTimeToLive = requireNonNull(printKeyCacheTimeToLive, "printKeyCacheTimeToLive can not be null");
        this.eventLogger = requireNonNull(eventLogger, "eventLogger can not be null");
        this.clock = clock;
        this.failOnHtmlDiff = failOnHtmlDiff;
    }

}
