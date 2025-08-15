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
package no.digipost.api.client.internal.http.response.interceptor;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.representations.EntryPoint;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static co.unruly.matchers.Java8Matchers.where;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResponseSignatureFilterTest {
    private final ResponseSignatureInterceptor interceptor = new ResponseSignatureInterceptor(EntryPoint::new);

    @Test
    public void skal_kaste_feil_om_server_signatur_mangler(@Mock HttpResponse response, @Mock EntityDetails entityDetails, @Mock HttpContext httpContext) {
        DigipostClientException thrown = assertThrows(DigipostClientException.class, () -> interceptor.process(response, entityDetails, httpContext));
        assertThat(thrown, where(Exception::getMessage, containsString("Missing X-Digipost-Signature header")));
    }

    @Test
    public void skal_ikke_kaste_feil_om_server_signatur_mangler_for_kall_som_eksplisitt_ikke_krever_signatur(@Mock HttpResponse response, @Mock EntityDetails entityDetails, @Mock HttpContext httpContext) {
        when(httpContext.getAttribute(ResponseSignatureInterceptor.NOT_SIGNED_RESPONSE)).thenReturn(true);
        assertDoesNotThrow(() -> interceptor.process(response, entityDetails, httpContext));
    }
}
