
package no.digipost.api.client.security;

import org.apache.http.Header;
import org.apache.http.HttpRequest;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClientRequestToSign2 implements RequestToSign {

	private final HttpRequest clientRequest;

	public ClientRequestToSign2(final HttpRequest httpRequest) {
		this.clientRequest = httpRequest;
	}

	@Override
	public String getMethod() {
		return clientRequest.getRequestLine().getMethod();
	}

	@Override
	public SortedMap<String, String> getHeaders() {
		TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
		Header[] headers = clientRequest.getAllHeaders();
		for (Header header : headers) {
			sortedHeaders.put(header.getName(), header.getValue());
		}
		return sortedHeaders;
	}

	@Override
	public String getPath() {
		try {
			String path = new URI(clientRequest.getRequestLine().getUri()).getPath();
			return path != null ? path : "";
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String getParameters() {
		int index = clientRequest.getRequestLine().getUri().indexOf('?');
		if(index == -1){
			return "";
		}
		String query = clientRequest.getRequestLine().getUri().substring(index + 1);
		return query != null ? query : "";
	}

}