package no.digipost.api.client.security;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;

import java.util.SortedMap;
import java.util.TreeMap;

public class ClientResponseToVerify2 implements ResponseToVerify{

	private final HttpContext context;
	private final HttpResponse response;

	public ClientResponseToVerify2(final HttpContext context, final HttpResponse response) {
		this.context = context;
		this.response = response;
	}

	@Override
	public int getStatus() {
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public SortedMap<String, String> getHeaders() {
		TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
		for(Header header : response.getAllHeaders()){
			sortedHeaders.put(header.getName(), header.getValue());
		}

		return sortedHeaders;
	}

	@Override
	public String getPath() {
		HttpRequestWrapper attribute = (HttpRequestWrapper)context.getAttribute("http.request");
		return attribute.getURI().getPath();
	}
}
