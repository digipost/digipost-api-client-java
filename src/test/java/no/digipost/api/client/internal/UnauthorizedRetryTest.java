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
package no.digipost.api.client.internal;

import no.digipost.api.client.BrokerId;
import no.digipost.api.client.DigipostClientConfig;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.DigipostUri;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.ErrorType;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Relation;
import no.digipost.api.client.security.jwt.MutualTlsTokenProvider;
import no.digipost.http.client.HttpClientFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.digipost.api.client.DigipostClientConfig.newConfiguration;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.apache.hc.core5.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that the JWT-authenticating client reacts to a rejected access token the way it is
 * supposed to: by discarding the token, fetching a new one and sending the request once more.
 * <p>
 * This goes through the fully wired client, and not just the individual pieces, because what it
 * needs to establish is that they are placed correctly relative to each other in the execution
 * chain, i.e. that the retry re-runs the request interceptors, and that the response
 * verifications do not fail the 401 before the retry gets to see it.
 */
public class UnauthorizedRetryTest {

    private static final BrokerId BROKER_ID = BrokerId.of(1234);

    private DigipostApiStub digipostApi;
    private MutualTlsTokenProvider tokenProvider;
    private ApiServiceImpl apiService;

    @BeforeEach
    void startApiAndBuildClient() throws Exception {
        digipostApi = new DigipostApiStub();
        tokenProvider = mock(MutualTlsTokenProvider.class);
        when(tokenProvider.getSslContext()).thenReturn(javax.net.ssl.SSLContext.getDefault());

        DigipostClientConfig config = newConfiguration().digipostApiUri(digipostApi.uri()).build();
        apiService = ApiServiceImpl.withMutualTlsTokenProvider(config, HttpClientFactory.createDefaultBuilder(), BROKER_ID, tokenProvider);
    }

    @AfterEach
    void closeClientAndStopApi() {
        if (apiService != null) {
            apiService.close();
        }
        if (digipostApi != null) {
            digipostApi.close();
        }
    }

    @Test
    void henter_nytt_token_og_sender_requesten_paa_nytt_naar_det_forrige_blir_avvist() {
        when(tokenProvider.getToken()).thenReturn("rejected-token", "fresh-token");
        digipostApi.respondWith(unauthorizedByGateway(), entryPoint());

        assertThat(apiService.getEntryPoint().getCertificate(), is("the-certificate"));

        verify(tokenProvider).invalidate("rejected-token");
        assertThat(digipostApi.receivedAuthorizationHeaders(), contains("Bearer rejected-token", "Bearer fresh-token"));
    }

    @Test
    void gir_opp_naar_ogsaa_det_nye_tokenet_blir_avvist() {
        when(tokenProvider.getToken()).thenReturn("rejected-token", "also-rejected-token");
        digipostApi.respondWith(unauthorized(), unauthorized());

        DigipostClientException thrown = assertThrows(DigipostClientException.class, () -> apiService.getEntryPoint());

        assertThat("den faktiske feilen fra apiet skal nå fram, ikke en signaturfeil på det usignerte 401-svaret",
                thrown.getErrorCode(), is(ErrorCode.UNKNOWN_USER_ID));
        assertThat("requesten skal sendes én gang til, ikke i det uendelige",
                digipostApi.receivedAuthorizationHeaders(), contains("Bearer rejected-token", "Bearer also-rejected-token"));
    }

    @Test
    void roerer_ikke_tokenet_naar_apiet_svarer_som_normalt() {
        when(tokenProvider.getToken()).thenReturn("the-token");
        digipostApi.respondWith(entryPoint());

        apiService.getEntryPoint();

        verify(tokenProvider, never()).invalidate(org.mockito.ArgumentMatchers.anyString());
        assertThat(digipostApi.receivedAuthorizationHeaders(), contains("Bearer the-token"));
    }

    /**
     * A 401 from in front of the Digipost application, i.e. one that is neither dated, hashed
     * nor signed the way the client expects a response from the application itself to be.
     */
    private static DigipostApiStub.StubbedResponse unauthorizedByGateway() {
        return DigipostApiStub.withoutDigipostHeaders(SC_UNAUTHORIZED, "{\"error\":\"invalid_token\"}");
    }

    private static DigipostApiStub.StubbedResponse unauthorized() {
        return DigipostApiStub.marshalled(SC_UNAUTHORIZED, new ErrorMessage(ErrorType.CLIENT_TECHNICAL, "UNKNOWN_USER_ID", "Unknown access token"));
    }

    private static DigipostApiStub.StubbedResponse entryPoint() {
        return DigipostApiStub.marshalled(SC_OK, new EntryPoint("the-certificate",
                new Link(Relation.SEARCH, new DigipostUri("/recipients/search"))));
    }
}
