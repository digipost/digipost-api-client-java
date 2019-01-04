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
package no.digipost.api.client.internal.http.response.interceptor;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.internal.http.response.interceptor.ResponseSignatureInterceptor;
import no.digipost.api.client.representations.EntryPoint;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

import static co.unruly.matchers.Java8Matchers.where;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;

public class ResponseSignatureFilterTest {

    @Rule
    public final MockitoRule mockito = MockitoJUnit.rule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private HttpResponse httpResponseMock;

    @Mock
    private HttpContext httpContextMock;

    private final ResponseSignatureInterceptor interceptor = new ResponseSignatureInterceptor(EntryPoint::new);

    @Test
    public void skal_kaste_feil_om_server_signatur_mangler() throws IOException, HttpException {
        expectedException.expect(DigipostClientException.class);
        expectedException.expect(where(Exception::getMessage, containsString("Missing X-Digipost-Signature header")));
        interceptor.process(httpResponseMock, httpContextMock);
    }

    @Test
    public void skal_ikke_kaste_feil_om_server_signatur_mangler_for_kall_som_eksplisitt_ikke_krever_signatur() throws IOException, HttpException {
        when(httpContextMock.getAttribute(ResponseSignatureInterceptor.NOT_SIGNED_RESPONSE)).thenReturn(true);
        interceptor.process(httpResponseMock, httpContextMock);
    }
}
