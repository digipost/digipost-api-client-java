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
package no.digipost.api.client.userdocuments;

import no.digipost.api.client.ApiCommons;
import no.digipost.api.client.filters.request.RequestContentSHA256Filter;
import no.digipost.api.client.filters.request.RequestDateInterceptor;
import no.digipost.api.client.filters.request.RequestSignatureInterceptor;
import no.digipost.api.client.filters.request.RequestUserAgentInterceptor;
import no.digipost.api.client.filters.response.ResponseContentSHA256Interceptor;
import no.digipost.api.client.filters.response.ResponseDateInterceptor;
import no.digipost.api.client.filters.response.ResponseSignatureInterceptor;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.security.Pkcs12KeySigner;
import no.digipost.api.client.util.Supplier;
import no.digipost.http.client.DigipostHttpClientFactory;
import no.digipost.http.client.DigipostHttpClientSettings;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * API client for managing Digipost documents on behalf of users
 */
public class DigipostUserDocumentClient {

	private final ApiService apiService;

	public DigipostUserDocumentClient(final ApiService apiService) {
		this.apiService = apiService;
	}

	public IdentificationResult identifyUser(final UserId userId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.identifyUser(userId);
			}
		}, IdentificationResult.class);
	}

	public void createAgreement(final Agreement agreement) {
		handleVoid(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.createAgreement(agreement);
			}
		});
	}

	public List<Agreement> getAgreements(final UserId userId) {
		final Agreements agreements = handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getAgreements(userId);
			}
		}, Agreements.class);
		return agreements.getAgreements();
	}

	private <T> T handle(final Callable<CloseableHttpResponse> action, Class<T> resultType) {
		try (final CloseableHttpResponse response = action.call()) {
			ApiCommons.checkResponse(response);
			return JAXB.unmarshal(response.getEntity().getContent(), resultType);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void handleVoid(final Callable<CloseableHttpResponse> action) {
		try (final CloseableHttpResponse response = action.call()) {
			ApiCommons.checkResponse(response);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public List<Document> getDocuments() {
		return null;
	}

	public static class Builder {
		private static final String PRODUCTION_ENDPOINT = "https://api.digipost.no";

		private URI serviceEndpoint;
		private final long accountId;
		private final InputStream certificateP12File;
		private final String certificatePassword;
		private HttpClientBuilder httpClientBuilder;
		private HttpHost proxyHost;

		public Builder(long accountId, InputStream certificateP12File, String certificatePassword){
			this.accountId = accountId;
			this.certificateP12File = certificateP12File;
			this.certificatePassword = certificatePassword;
			try {
				serviceEndpoint(new URI(PRODUCTION_ENDPOINT));
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
			httpClientBuilder = DigipostHttpClientFactory.createBuilder(DigipostHttpClientSettings.DEFAULT);
		}

		public Builder useProxy(final HttpHost proxyHost) {
			this.proxyHost = proxyHost;
			return this;
		}

		public Builder serviceEndpoint(URI endpointUri) {
			this.serviceEndpoint = endpointUri;
			return this;
		}

		public DigipostUserDocumentClient build() {
			final ApiServiceProvider apiServiceProvider = new ApiServiceProvider();
			final ResponseSignatureInterceptor responseSignatureInterceptor = new ResponseSignatureInterceptor(new Supplier<byte[]>() {
				@Override
				public byte[] get() {
					return apiServiceProvider.getApiService().getEntryPoint().getCertificate().getBytes();
				}
			});

			httpClientBuilder.addInterceptorLast(new RequestDateInterceptor(null));
			httpClientBuilder.addInterceptorLast(new RequestUserAgentInterceptor());
			httpClientBuilder.addInterceptorLast(new RequestSignatureInterceptor(new Pkcs12KeySigner(certificateP12File, certificatePassword), null, new RequestContentSHA256Filter(null)));
			httpClientBuilder.addInterceptorLast(new ResponseDateInterceptor());
			httpClientBuilder.addInterceptorLast(new ResponseContentSHA256Interceptor());
			httpClientBuilder.addInterceptorLast(responseSignatureInterceptor);

			if (proxyHost != null) {
				httpClientBuilder.setProxy(proxyHost);
			}

			final ApiService apiService = new ApiService(serviceEndpoint, accountId, httpClientBuilder.build());
			apiServiceProvider.setApiService(apiService);
			return new DigipostUserDocumentClient(apiService);
		}
	}
}
