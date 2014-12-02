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
package no.digipost.api.client.filters;

public class LoggingFilter { }

/*

	private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

	private static final String NOTIFICATION_PREFIX = "* ";

	private static final String REQUEST_PREFIX = "> ";

	private static final String RESPONSE_PREFIX = "< ";

	private final class Adapter extends AbstractClientRequestAdapter {
		private final StringBuilder b;

		Adapter(ClientRequestAdapter cra, StringBuilder b) {
			super(cra);
			this.b = b;
		}

		public OutputStream adapt(ClientRequest request, OutputStream out) throws IOException {
			return new LoggingOutputStream(getAdapter().adapt(request, out), b);
		}

	}

	private final class LoggingOutputStream extends OutputStream {
		private final OutputStream out;

		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		private final StringBuilder b;

		LoggingOutputStream(OutputStream out, StringBuilder b) {
			this.out = out;
			this.b = b;
		}

		@Override
		public void write(byte[] b)  throws IOException {
			baos.write(b);
			out.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len)  throws IOException {
			baos.write(b, off, len);
			out.write(b, off, len);
		}

		@Override
		public void write(int b) throws IOException {
			baos.write(b);
			out.write(b);
		}

		@Override
		public void close() throws IOException {
			printEntity(b, baos.toByteArray());
			log(b);
			out.close();
		}
	}

	private final PrintStream loggingStream;

	private final Logger logger;

	private long _id = 0;

	public LoggingFilter() {
		this(LOGGER);
	}

	public LoggingFilter(Logger logger) {
		this.loggingStream = null;
		this.logger = logger;
	}

	public LoggingFilter(PrintStream loggingStream) {
		this.loggingStream = loggingStream;
		this.logger = null;
	}

	private void log(StringBuilder b) {
		if (logger != null) {
			logger.info(b.toString());
		} else {
			loggingStream.print(b);
		}
	}

	private StringBuilder prefixId(StringBuilder b, long id) {
		b.append(Long.toString(id)).append(" ");
		return b;
	}

	@Override
	public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
		long id = ++this._id;

		logRequest(id, request);

		ClientResponse response = getNext().handle(request);

		logResponse(id, response);

		return response;
	}

	private void logRequest(long id, ClientRequest request) {
		StringBuilder b = new StringBuilder();

		printRequestLine(b, id, request);
		printRequestHeaders(b, id, request.getHeaders());

		if (request.getEntity() != null) {
			request.setAdapter(new Adapter(request.getAdapter(), b));
		} else {
			log(b);
		}
	}

	private void printRequestLine(StringBuilder b, long id, ClientRequest request) {
		prefixId(b, id).append(NOTIFICATION_PREFIX).append("Client out-bound request").append("\n");
		prefixId(b, id).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").
				append(request.getURI().toASCIIString()).append("\n");
	}

	private void printRequestHeaders(StringBuilder b, long id, MultivaluedMap<String, Object> headers) {
		for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
			List<Object> val = e.getValue();
			String header = e.getKey();

			if(val.size() == 1) {
				prefixId(b, id).append(REQUEST_PREFIX).append(header).append(": ").append(ClientRequest.getHeaderValue(val.get(0))).append("\n");
			} else {
				StringBuilder sb = new StringBuilder();
				boolean add = false;
				for(Object o : val) {
					if(add) sb.append(',');
					add = true;
					sb.append(ClientRequest.getHeaderValue(o));
				}
				prefixId(b, id).append(REQUEST_PREFIX).append(header).append(": ").append(sb.toString()).append("\n");
			}
		}
	}

	private void logResponse(long id, ClientResponse response) {
		StringBuilder b = new StringBuilder();

		printResponseLine(b, id, response);
		printResponseHeaders(b, id, response.getHeaders());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = response.getEntityInputStream();
		try {
			ReaderWriter.writeTo(in, out);

			byte[] requestEntity = out.toByteArray();
			printEntity(b, requestEntity);
			response.setEntityInputStream(new ByteArrayInputStream(requestEntity));
		} catch (IOException ex) {
			throw new ClientHandlerException(ex);
		}
		log(b);
	}

	private void printResponseLine(StringBuilder b, long id, ClientResponse response) {
		prefixId(b, id).append(NOTIFICATION_PREFIX).
				append("Client in-bound response").append("\n");
		prefixId(b, id).append(RESPONSE_PREFIX).
				append(Integer.toString(response.getStatus())).
				append("\n");
	}

	private void printResponseHeaders(StringBuilder b, long id, MultivaluedMap<String, String> headers) {
		for (Map.Entry<String, List<String>> e : headers.entrySet()) {
			String header = e.getKey();
			for (String value : e.getValue()) {
				prefixId(b, id).append(RESPONSE_PREFIX).append(header).append(": ").
						append(value).append("\n");
			}
		}
		prefixId(b, id).append(RESPONSE_PREFIX).append("\n");
	}

	private void printEntity(StringBuilder b, byte[] entity) throws IOException {
		if (entity.length == 0)
			return;
		CharsetEncoder encoder = Charset.forName("utf-8").newEncoder();
		String str = new String(entity);
		if (encoder.canEncode(str)) {
			b.append(str).append("\n");
		} else {
			b.append("<<<binary data>>>\n");
		}
	}
}

*/