package no.digipost.api.client.util;

import no.digipost.api.client.readers.TextErrorMessageBodyReader;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class JerseyClientProvider {
	private static final Integer CONNECTION_TIMEOUT = 60000;
	private static final Integer READ_TIMEOUT = 60000;

	public static Client newClient() {
		return newClient(CONNECTION_TIMEOUT, READ_TIMEOUT);
	}

	public static Client newClient(final int connectionTimeout, final int readTimout) {
		ClientConfig config = new ClientConfig();
		config.register(MultiPartFeature.class);
		config.register(TextErrorMessageBodyReader.class);
		config.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
		config.property(ClientProperties.READ_TIMEOUT, readTimout);

		return ClientBuilder.newClient(config);
	}
}
