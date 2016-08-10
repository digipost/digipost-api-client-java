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
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.filters.request.RequestContentSHA256Filter;
import no.digipost.api.client.filters.request.RequestDateInterceptor;
import no.digipost.api.client.filters.request.RequestSignatureInterceptor;
import no.digipost.api.client.filters.request.RequestUserAgentInterceptor;
import no.digipost.api.client.filters.response.ResponseContentSHA256Interceptor;
import no.digipost.api.client.filters.response.ResponseDateInterceptor;
import no.digipost.api.client.filters.response.ResponseSignatureInterceptor;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.security.Pkcs12KeySigner;
import no.digipost.api.client.util.Supplier;
import no.digipost.http.client.DigipostHttpClientFactory;
import no.digipost.http.client.DigipostHttpClientSettings;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * API client for managing Digipost documents on behalf of users
 */
public class DigipostUserDocumentClient {

	private final ApiService apiService;

	public DigipostUserDocumentClient(final ApiService apiService) {
		this.apiService = apiService;
		CryptoUtil.verifyTLSCiphersAvailable();
	}

	public IdentificationResult identifyUser(final SenderId senderId, final UserId userId) {
		return identifyUser(senderId, userId, null); }

	public IdentificationResult identifyUser(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.identifyUser(senderId, userId, requestTrackingId);
			}
		}, IdentificationResult.class);
	}

	public URI createOrReplaceAgreement(final SenderId senderId, final Agreement agreement) {
		return createOrReplaceAgreement(senderId, agreement, null); }

	public URI createOrReplaceAgreement(final SenderId senderId, final Agreement agreement, final String requestTrackingId) {
		final CloseableHttpResponse response = apiService.createAgreement(senderId, agreement, requestTrackingId);
		ApiCommons.checkResponse(response);
		try {
			return new URI(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public Agreement getAgreement(final URI agreementUri, final String requestTrackingId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getAgreement(agreementUri, requestTrackingId);
			}
		}, Agreement.class);
	}

	public Agreement getAgreement(final SenderId senderId, final AgreementType type, final UserId userId, final String requestTrackingId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getAgreement(senderId, type, userId, requestTrackingId);
			}
		}, Agreement.class);
	}

	public List<Agreement> getAgreements(final SenderId senderId, final UserId userId) {
		return getAgreements(senderId, userId, null);
	}

	public List<Agreement> getAgreements(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		final Agreements agreements = handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getAgreements(senderId, userId, requestTrackingId);
			}
		}, Agreements.class);
		return agreements.getAgreements();
	}

	public void deleteAgreement(final URI agreementPath, final String requestTrackingId) {
		handleVoid(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.deleteAgrement(agreementPath, requestTrackingId);
			}
		});
	}

	public void deleteAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		handleVoid(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.deleteAgrement(senderId, agreementType, userId, requestTrackingId);
			}
		});
	}

	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId) {
		return getDocuments(senderId, agreementType, userId, null);
	}

	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		final Documents documents = handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getDocuments(senderId, agreementType, userId, requestTrackingId);
			}
		}, Documents.class);
		return documents.getDocuments();
	}

	public Document getDocument(final SenderId senderId, final long documentId) {
		return getDocument(senderId, documentId, null);
	}

	public Document getDocument(final SenderId senderId, final long documentId, final String requestTrackingId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getDocument(senderId, documentId, requestTrackingId);
			}
		}, Document.class);
	}

	public Document updateInvoice(final SenderId senderId, final long documentId, final Invoice invoice) {
		return updateInvoice(senderId, documentId, invoice, null);
	}

	public Document updateInvoice(final SenderId senderId, final long documentId, final Invoice invoice, final String requestTrackingId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.updateInvoice(senderId, documentId, invoice, requestTrackingId);
			}
		}, Document.class);
	}

	private <T> T handle(final Callable<CloseableHttpResponse> action, Class<T> resultType) {
		try (final CloseableHttpResponse response = action.call()) {
			ApiCommons.checkResponse(response);
			return JAXB.unmarshal(response.getEntity().getContent(), resultType);
		} catch (DigipostClientException e) {
			throw e;
		} catch (Exception e) {
			throw new DigipostClientException(ErrorCode.CLIENT_ERROR, e);
		}
	}

	private void handleVoid(final Callable<CloseableHttpResponse> action) {
		try (final CloseableHttpResponse response = action.call()) {
			ApiCommons.checkResponse(response);
		} catch (DigipostClientException e) {
			throw e;
		} catch (Exception e) {
			throw new DigipostClientException(ErrorCode.CLIENT_ERROR, e);
		}
	}

	public static class Builder {

		private static final URI PRODUCTION_ENDPOINT = URI.create("https://api.digipost.no");

		private URI serviceEndpoint;
		private final BrokerId brokerId;
		private final InputStream certificateP12File;
		private final String certificatePassword;
		private HttpClientBuilder httpClientBuilder;
		private HttpHost proxyHost;
		private PrivateKey privateKey;

		public Builder(final BrokerId brokerId, InputStream certificateP12File, String certificatePassword){
			this(brokerId, certificateP12File, certificatePassword, null);
		}

		public Builder(BrokerId brokerId, PrivateKey privateKey) {
			this(brokerId, null, null, privateKey);
		}

		private Builder(BrokerId brokerId, InputStream certificateP12File, String certificatePassword, PrivateKey privateKey) {
			this.brokerId = brokerId;
			if (privateKey == null && (certificateP12File == null || certificatePassword == null)) {
				throw new IllegalArgumentException("Client must be supplied either PrivateKey, or Certificate and password for certificate");
			}
			this.certificateP12File = certificateP12File;
			this.certificatePassword = certificatePassword;
			this.privateKey = privateKey;
			serviceEndpoint(PRODUCTION_ENDPOINT);
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

		public Builder veryDangerouslyDisableCertificateVerificationWhichIsAbsolutelyUnfitForProductionCode() {
			if (this.serviceEndpoint.compareTo(PRODUCTION_ENDPOINT) == 0) {
				throw new RuntimeException("You should never ever disable certificate verification when connecting to the production endpoint");
			}
			SSLContextBuilder sslContextBuilder= new SSLContextBuilder();
			try {
				sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build(), new HostnameVerifier() {
					@Override
					public boolean verify(String s, SSLSession sslSession) {
						return true;
					}
				});
				httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
			} catch (Exception e) {
				throw new RuntimeException("Could not disable certificate verification: " + e);
			}
			System.err.println("Not checking validity of certificates for any hostnames");
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
			if (privateKey == null) {
				httpClientBuilder.addInterceptorLast(new RequestSignatureInterceptor(new Pkcs12KeySigner(certificateP12File, certificatePassword), null, new RequestContentSHA256Filter(null)));
			} else {
				httpClientBuilder.addInterceptorLast(new RequestSignatureInterceptor(new Pkcs12KeySigner(privateKey), null, new RequestContentSHA256Filter(null)));
			}
			httpClientBuilder.addInterceptorLast(new ResponseDateInterceptor());
			httpClientBuilder.addInterceptorLast(new ResponseContentSHA256Interceptor());
			httpClientBuilder.addInterceptorLast(responseSignatureInterceptor);

			if (proxyHost != null) {
				httpClientBuilder.setProxy(proxyHost);
			}

			final ApiService apiService = new ApiService(serviceEndpoint, brokerId, httpClientBuilder.build());
			apiServiceProvider.setApiService(apiService);
			return new DigipostUserDocumentClient(apiService);
		}
	}
}
