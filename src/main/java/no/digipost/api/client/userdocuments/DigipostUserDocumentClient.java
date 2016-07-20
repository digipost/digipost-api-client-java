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
import no.digipost.api.client.security.Pkcs12KeySigner;
import no.digipost.api.client.util.Supplier;
import no.digipost.http.client.DigipostHttpClientFactory;
import no.digipost.http.client.DigipostHttpClientSettings;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.conn.ssl.SSLContextBuilder;

import javax.crypto.Cipher;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
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
		// TODO: should this be more elegantly handled?
		try {
			int keyLength = Cipher.getMaxAllowedKeyLength("AES");
			if (keyLength < 256) {
				throw new DigipostClientException(ErrorCode.CLIENT_ERROR, "FATAL: System does not support large enough keys. HINT: is the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy installed on the system?");
			}
		} catch (NoSuchAlgorithmException e) {
			throw new DigipostClientException(ErrorCode.CLIENT_ERROR, "FATAL: System does not support AES. HINT: is the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy installed on the system?");
		}
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

	public List<Document> getDocuments(final UserId userId, final AgreementType agreementType) {
		final Documents documents = handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getDocuments(userId, agreementType);
			}
		}, Documents.class);
		return documents.getDocuments();
	}

	public Document getDocument(final long documentId) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.getDocument(documentId);
			}
		}, Document.class);
	}

	public Document updateInvoice(final long documentId, final Invoice invoice) {
		return handle(new Callable<CloseableHttpResponse>() {
			@Override
			public CloseableHttpResponse call() throws Exception {
				return apiService.updateInvoice(documentId, invoice);
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
		private final long accountId;
		private final InputStream certificateP12File;
		private final String certificatePassword;
		private HttpClientBuilder httpClientBuilder;
		private HttpHost proxyHost;
		private PrivateKey privateKey;

		public Builder(long accountId, InputStream certificateP12File, String certificatePassword){
			this(accountId, certificateP12File, certificatePassword, null);
		}

		public Builder(long accountId, PrivateKey privateKey) {
			this(accountId, null, null, privateKey);
		}

		private Builder(long accountId, InputStream certificateP12File, String certificatePassword, PrivateKey privateKey) {
			this.accountId = accountId;
			if (certificateP12File == null && certificatePassword == null && privateKey == null) {
				throw new IllegalArgumentException();
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

			final ApiService apiService = new ApiService(serviceEndpoint, accountId, httpClientBuilder.build(), proxyHost);
			apiServiceProvider.setApiService(apiService);
			return new DigipostUserDocumentClient(apiService);
		}
	}
}
