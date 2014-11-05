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
package no.digipost.api.client.filters;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.filters.response.ResponseDateFilter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDateFilterTest {

	private ResponseDateFilter responseDateFilter;

	@Mock
	private ClientResponseContext clientResponseMock;

	@Mock
	private ClientRequestContext clientRequestMock;

	@Before
	public void setUp() {
		responseDateFilter = new ResponseDateFilter();
		responseDateFilter.setThrowOnError(true);
	}

	@Test
	public void skal_kaste_exception_når_Date_header_mangler() throws IOException {
		when(clientResponseMock.getHeaders()).thenReturn(new MultivaluedHashMap<String, String>());
		try {
			responseDateFilter.filter(clientRequestMock, clientResponseMock);
			fail("Skulle ha kastet feil grunnet manglende Date-header");
		} catch (DigipostClientException e) {
			assertThat(e.getMessage(), containsString("Mangler Date-header"));
		}
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_på_feil_format() throws IOException {
		MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("Date", "16. januar 2012 - 16:14:23");
		when(clientResponseMock.getHeaders()).thenReturn(headers);
		try {
			responseDateFilter.filter(clientRequestMock, clientResponseMock);
			fail("Skulle kastet feil grunnet feilaktig Date header format");
		} catch (DigipostClientException e) {
			assertThat(e.getMessage(), containsString("Date-header kunne ikke parses"));
		}
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_for_ny() throws IOException {
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2014, 11, 4, 21, 00, 58, UTC).getMillis());
		MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("Date", "Tue, 04 Nov 2014 21:10:58 GMT");
		when(clientResponseMock.getHeaders()).thenReturn(headers);
		try {
			responseDateFilter.filter(clientRequestMock, clientResponseMock);
			fail("Skulle kastet feil grunnet for ny Date header");
		} catch (DigipostClientException e) {
			assertThat(e.getMessage(), containsString("Date-header fra server er for ny"));
		}
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_for_gammel() throws IOException {
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2014, 11, 4, 21, 20, 58, UTC).getMillis());
		MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("Date", "Tue, 04 Nov 2014 21:10:58 GMT");
		when(clientResponseMock.getHeaders()).thenReturn(headers);
		try {
			responseDateFilter.filter(clientRequestMock, clientResponseMock);
			fail("Skulle kastet feil grunnet for gammel Date header");
		} catch (DigipostClientException e) {
			assertThat(e.getMessage(), containsString("Date-header fra server er for gammel"));
		}
	}

	@Test
	public void skal_ikke_kaste_feil_om_vi_ikke_vil_det() throws IOException {
		responseDateFilter.setThrowOnError(false);
		when(clientResponseMock.getHeaders()).thenReturn(new MultivaluedHashMap<String, String>());
		responseDateFilter.filter(clientRequestMock, clientResponseMock);
	}

}
