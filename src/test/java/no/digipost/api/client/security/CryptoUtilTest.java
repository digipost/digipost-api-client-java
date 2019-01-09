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
package no.digipost.api.client.security;

import org.junit.jupiter.api.Test;

import java.security.PrivateKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CryptoUtilTest {

    @Test
    public void shouldLoadPrivateKeyFromPKCS12File() {
        final PrivateKey privateKey = CryptoUtil.loadKeyFromP12(getClass().getResourceAsStream("certificate.p12"), "Qwer12345");
        assertThat(privateKey, notNullValue());
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenBadPassword() {
        assertThrows(RuntimeException.class, () -> CryptoUtil.loadKeyFromP12(getClass().getResourceAsStream("certificate.p12"), ""));
    }
}
