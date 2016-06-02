package no.digipost.api.client;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OverriddenMultipartSubtypeHttpEntity implements HttpEntity {
	private final HttpEntity entity;
	private final String subType;

	public OverriddenMultipartSubtypeHttpEntity(HttpEntity entity, String subType) {
		this.entity = entity;
		this.subType = subType;
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
		final Header originalContentType = entity.getContentType();
		return new BasicHeader(originalContentType.getName(), originalContentType.getValue().replaceFirst("form-data", subType));
	}

	@Override
	public Header getContentEncoding() {
		return entity.getContentEncoding();
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return entity.getContent();
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		entity.writeTo(outputStream);
	}

	@Override
	public boolean isStreaming() {
		return entity.isStreaming();
	}

	@Override
	@Deprecated
	public void consumeContent() throws IOException {
		entity.consumeContent();
	}
}
