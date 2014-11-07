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
import no.digipost.api.client.filters.response.ResponseSignatureFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseSignatureFilterTest {

	private ResponseSignatureFilter responseSignatureFilter;

	@Mock
	private ApiService apiServiceMock;

	@Mock
	private ClientRequestContext clientRequestMock;

	@Mock
	private ClientResponseContext clientResponseMock;

	@Before
	public void setUp() throws URISyntaxException {
		responseSignatureFilter = new ResponseSignatureFilter(apiServiceMock);
		responseSignatureFilter.setThrowOnError(true);
		when(clientRequestMock.getUri()).thenReturn(new URI("/some/resource"));
	}

	@Test
	public void skal_ikke_kaste_feil_om_vi_ikke_vil_det() throws IOException {
		responseSignatureFilter.setThrowOnError(false);
		responseSignatureFilter.filter(clientRequestMock, clientResponseMock);
	}

	@Test
	public void skal_kaste_feil_om_server_signatur_mangler() throws IOException {
		when(clientResponseMock.getHeaders()).thenReturn(new MultivaluedHashMap<String, String>());
		try {
			responseSignatureFilter.filter(clientRequestMock, clientResponseMock);
			fail("Skulle kastet feil grunnet manglende signatur header");
		} catch (DigipostClientException e) {
			assertThat(e.getMessage(), containsString("Mangler X-Digipost-Signature-header"));
		}
	}

}
