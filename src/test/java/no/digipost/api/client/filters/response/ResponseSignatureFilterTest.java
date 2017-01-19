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
package no.digipost.api.client.filters.response;

import no.digipost.api.client.ApiService;
import no.digipost.api.client.errorhandling.DigipostClientException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ResponseSignatureFilterTest {

    @Rule
    public final MockitoRule mockito = MockitoJUnit.rule();

	private ResponseSignatureInterceptor responseSignatureInterceptor;

	@Mock
	private ApiService apiServiceMock;

	@Mock
	private HttpResponse httpResponseMock;

	@Mock
	private HttpContext httpContextMock;

	@Before
	public void setUp() throws URISyntaxException {
		responseSignatureInterceptor = new ResponseSignatureInterceptor(apiServiceMock);
		responseSignatureInterceptor.setThrowOnError(true);
		when(httpContextMock.getAttribute(anyString())).thenReturn(new CookieOrigin("host", 123, "/some/resource", true));
	}

	@Test
	public void skal_ikke_kaste_feil_om_vi_ikke_vil_det() throws IOException, HttpException {
		responseSignatureInterceptor.setThrowOnError(false);
		responseSignatureInterceptor.process(httpResponseMock, httpContextMock);
	}

	@Test
	public void skal_kaste_feil_om_server_signatur_mangler() throws IOException, HttpException {
		try {
			responseSignatureInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle kastet feil grunnet manglende signatur header");
		} catch (DigipostClientException e) {
			assertThat(e.getMessage(), containsString("Mangler X-Digipost-Signature-header"));
		}
	}

}
