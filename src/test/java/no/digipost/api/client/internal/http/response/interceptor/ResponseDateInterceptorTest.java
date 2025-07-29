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
import no.digipost.api.client.internal.http.StatusLineMock;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResponseDateInterceptorTest {

    private static final ZoneId UTC = ZoneId.of("UTC");
    private static final ZonedDateTime NOW = ZonedDateTime.of(2014, 11, 4, 21, 10, 58, 0, UTC);
    private final Clock clock = Clock.fixed(NOW.toInstant(), UTC);

    private ResponseDateInterceptor responseDateInterceptor;

    @Mock
    private HttpContext httpContextMock;

    @Mock
    private HttpResponse httpResponseMock;

    @BeforeEach
    public void setUp() {
        responseDateInterceptor = new ResponseDateInterceptor(clock);
    }

    @Test
    public void skal_kaste_exception_når_Date_header_mangler() throws IOException, HttpException {
        when(httpResponseMock.getStatusLine()).thenReturn(new StatusLineMock(200));
        try {
            responseDateInterceptor.process(httpResponseMock, httpContextMock);
            fail("Skulle ha kastet feil grunnet manglende Date-header");
        } catch (DigipostClientException e) {
            assertThat(e.getMessage(), containsString("Missing Date header in response"));
        }
    }

    @Test
    public void skal_kaste_feil_når_Date_header_er_på_feil_format() throws IOException, HttpException {
        List<BasicHeader> headers = new ArrayList<>();
        headers.add(new BasicHeader("Date", "16. januar 2012 - 16:14:23"));
        when(httpResponseMock.getFirstHeader("Date")).thenReturn(headers.get(0));
        try {
            responseDateInterceptor.process(httpResponseMock, httpContextMock);
            fail("Skulle kastet feil grunnet feilaktig Date header format");
        } catch (DigipostClientException e) {
            assertThat(e.getMessage(), containsString("Unable to parse Date header '16. januar 2012 - 16:14:23'"));
        }
    }

    @Test
    public void skal_kaste_feil_når_Date_header_er_for_ny() throws IOException, HttpException {
        List<BasicHeader> headers = new ArrayList<>();
        headers.add(new BasicHeader("Date", "Tue, 04 Nov 2014 21:20:58 GMT"));
        when(httpResponseMock.getFirstHeader("Date")).thenReturn(headers.get(0));
        try {
            responseDateInterceptor.process(httpResponseMock, httpContextMock);
            fail("Skulle kastet feil grunnet for ny Date header");
        } catch (DigipostClientException e) {
            assertThat(e.getMessage(), containsString("Date-header from server is too early"));
        }
    }

    @Test
    public void skal_kaste_feil_når_Date_header_er_for_gammel() throws IOException, HttpException {
        List<BasicHeader> headers = new ArrayList<>();
        headers.add(new BasicHeader("Date", "Tue, 04 Nov 2014 21:00:58 GMT"));
        when(httpResponseMock.getFirstHeader("Date")).thenReturn(headers.get(0));
        try {
            responseDateInterceptor.process(httpResponseMock, httpContextMock);
            fail("Skulle kastet feil grunnet for gammel Date header");
        } catch (DigipostClientException e) {
            assertThat(e.getMessage(), containsString("Date header in response from server is too old"));
        }
    }
}
