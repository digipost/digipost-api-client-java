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
package no.digipost.api.client.filters.response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple client response filter that will log complete response entity
 */
@Provider
public class LoggingClientResponseFilter implements ClientResponseFilter {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingClientResponseFilter.class);

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		InputStream entityStream = responseContext.getEntityStream();
		byte[] bytes = IOUtils.toByteArray(entityStream);
		LOG.info(new String(bytes, "UTF8"));
		ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
		responseContext.setEntityStream(baos);
	}


}
