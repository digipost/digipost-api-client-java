package no.digipost.api.client.readers;

import no.digipost.api.client.representations.ErrorMessage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static javax.ws.rs.RuntimeType.CLIENT;
import static no.digipost.api.client.representations.ErrorType.SERVER;


@Consumes("text/*")
@Singleton
@ConstrainedTo(CLIENT)
public class TextErrorMessageBodyReader implements MessageBodyReader<ErrorMessage> {

	private static final Logger LOG = LoggerFactory.getLogger(TextErrorMessageBodyReader.class);

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == ErrorMessage.class;
	}

	@Override
	public ErrorMessage readFrom(Class<ErrorMessage> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		String errorMessage = IOUtils.toString(entityStream, "UTF8");
		LOG.info("Got unexpected Content-Type from server: {}", mediaType);
		LOG.info("HTTP response headers: {}", headers(httpHeaders));
		LOG.info("HTTP response body: {}", errorMessage);
		return new ErrorMessage(SERVER, errorMessage);
	}


	private String headers(MultivaluedMap<String, String> httpHeaders) {

		StringBuffer result = new StringBuffer();

		for (String key : httpHeaders.keySet()) {

			result.append(key).append(" :");

			List<String> values = httpHeaders.get(key);
			for (String value : values) {
				result.append(" ").append(value);
			}

			result.append("; ");

		}
		return result.toString();

	}

}
