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
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoggingClientResponseInterceptor implements HttpResponseInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingClientResponseInterceptor.class);

	@Override
	public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
		InputStream entityStream = response.getEntity().getContent();
		byte[] bytes = IOUtils.toByteArray(entityStream);
		LOG.info(new String(bytes, "UTF8"));
		ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
		response.setEntity(new ByteArrayEntity(bytes));
	}
}
