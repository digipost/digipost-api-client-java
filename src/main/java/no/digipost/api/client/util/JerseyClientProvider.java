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


import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JerseyClientProvider {

	private static final Integer THREADPOOL_SIZE = 100;
	private static final Integer CONNECTION_TIMEOUT = 60000;
	private static final Integer READ_TIMEOUT = 60000;

	public static Client newClient() {
		return newClient(CONNECTION_TIMEOUT, READ_TIMEOUT);
	}

	public static Client newClient(final int connectionTimeout, final int readTimout) {
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		//client.setConnectTimeout(connectionTimeout);
		//client.setReadTimeout(readTimout);
		//client.getProperties().put(ClientConfig.PROPERTY_THREADPOOL_SIZE, THREADPOOL_SIZE);
		return client;
	}
}	

