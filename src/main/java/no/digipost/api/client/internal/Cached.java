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
package no.digipost.api.client.internal;

import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.cache2.inmemory.Cache;
import no.digipost.cache2.inmemory.SingleCached;

import java.time.Duration;
import java.util.concurrent.Callable;

import static no.digipost.cache2.inmemory.CacheConfig.expireAfterAccess;
import static no.digipost.cache2.inmemory.CacheConfig.useSoftValues;

final class Cached {

    final SingleCached<EntryPoint> entryPoint;
    final Cache<String, SenderInformation> senderInformation;

    Cached(Callable<EntryPoint> entryPointFetcher) {
        this.entryPoint = new SingleCached<>("digipost-entrypoint", entryPointFetcher, expireAfterAccess(Duration.ofMinutes(5)), useSoftValues);
        this.senderInformation = Cache.create("sender-information", expireAfterAccess(Duration.ofMinutes(5)), useSoftValues);
    }

}
