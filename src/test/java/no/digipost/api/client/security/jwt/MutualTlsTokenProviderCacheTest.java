/*
 * Copyright (C) Posten Bring AS
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
package no.digipost.api.client.security.jwt;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;
import static no.digipost.api.client.security.jwt.MutualTlsTokenProvider.resolveCacheValidUntil;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class MutualTlsTokenProviderCacheTest {

    private static final Instant NOW = Instant.parse("2026-08-14T12:00:00Z");

    @Test
    public void refresher_tokenet_kort_foer_det_utloeper() {
        assertThat(resolveCacheValidUntil(NOW, NOW.plus(300, SECONDS)), is(NOW.plus(270, SECONDS)));
    }

    @Test
    public void cacher_kortlevde_tokens_i_stedet_for_aa_hente_nytt_per_request() {
        assertThat(resolveCacheValidUntil(NOW, NOW.plus(10, SECONDS)), is(NOW.plus(5, SECONDS)));
    }

    @Test
    public void cacher_aldri_lenger_enn_tokenet_er_gyldig() {
        assertThat(resolveCacheValidUntil(NOW, NOW.plus(3, SECONDS)), is(NOW.plus(3, SECONDS)));
    }

    @Test
    public void cacher_alltid_i_et_positivt_tidsrom() {
        assertThat(resolveCacheValidUntil(NOW, NOW.plus(31, SECONDS)), greaterThan(NOW));
        assertThat(resolveCacheValidUntil(NOW, NOW.plus(30, SECONDS)), greaterThan(NOW));
        assertThat(resolveCacheValidUntil(NOW, NOW.plus(1, SECONDS)), greaterThan(NOW));
    }
}
