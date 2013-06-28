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
package no.digipost.api.client.util;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.ApacheHttpClientHandler;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class JerseyClientProvider {

	private static final int MAX_HTTP_CONNECTIONS = 100;

	private static final Integer THREADPOOL_SIZE = 100;
	private static final Integer CONNECTION_TIMEOUT = 60000;
	private static final Integer READ_TIMEOUT = 60000;

	public static Client newClient() {
		ApacheHttpClientHandler clientHandler = new ApacheHttpClientHandler(opprettMultiThreadedHttpClient(), opprettClientConfig(CONNECTION_TIMEOUT,
				READ_TIMEOUT));
		return new ApacheHttpClient(clientHandler);
	}

	public static Client newClient(final int connectionTimeout, final int readTimout) {
		ApacheHttpClientHandler clientHandler = new ApacheHttpClientHandler(opprettMultiThreadedHttpClient(), opprettClientConfig(connectionTimeout,
				readTimout));
		return new ApacheHttpClient(clientHandler);
	}

	private static HttpClient opprettMultiThreadedHttpClient() {
		HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(MAX_HTTP_CONNECTIONS);
		params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, MAX_HTTP_CONNECTIONS);
		httpConnectionManager.setParams(params);
		return new HttpClient(httpConnectionManager);
	}

	private static ClientConfig opprettClientConfig(final Integer connectionTimeout, final Integer readTimeout) {
		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getProperties().put(ApacheHttpClientConfig.PROPERTY_THREADPOOL_SIZE, THREADPOOL_SIZE);
		config.getProperties().put(ApacheHttpClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeout);
		config.getProperties().put(ApacheHttpClientConfig.PROPERTY_READ_TIMEOUT, readTimeout);
		return config;
	}
}
