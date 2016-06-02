package no.digipost.api.client;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.*;

public class BiggerMultipartFormEntity implements HttpEntity {
	private HttpEntity entity;

	BiggerMultipartFormEntity(HttpEntity multipartFormEntity){
		this.entity = multipartFormEntity;
	}

	@Override
	public boolean isRepeatable() {
		return entity.isRepeatable();
	}

	@Override
	public boolean isChunked() {
		return entity.isChunked();
	}

	@Override
	public long getContentLength() {
		return entity.getContentLength();
	}

	@Override
	public Header getContentType() {
		return entity.getContentType();
	}

	@Override
	public Header getContentEncoding() {
		return entity.getContentEncoding();
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		writeTo(outstream);
		outstream.flush();
		return new ByteArrayInputStream(outstream.toByteArray());
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		this.entity.writeTo(outstream);
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void consumeContent() throws IOException {

	}
}
