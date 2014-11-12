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
package no.digipost.api.client.readers;

import no.digipost.api.client.representations.ErrorMessage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static javax.ws.rs.RuntimeType.CLIENT;
import static no.digipost.api.client.representations.ErrorType.SERVER;
import static no.digipost.api.client.util.LoggingUtil.headersAsString;


@Consumes("text/*")
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
		LOG.info("HTTP response headers: {}", headersAsString(httpHeaders));
		LOG.info("HTTP response body: {}", errorMessage);
		return new ErrorMessage(SERVER, errorMessage);
	}


}
