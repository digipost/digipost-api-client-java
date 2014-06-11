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
package no.digipost.api.client;

import no.digipost.api.client.representations.*;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;

public interface ApiService {
	EntryPoint getEntryPoint();

	Response multipartMessage(MultiPart multiPart);

	Response createMessage(Message message);

	Response fetchExistingMessage(URI location);

	Response getEncryptionKey(URI location);

	Response addContent(Document document, InputStream letterContent);

	Response send(MessageDelivery createdMessage);

	Recipients search(String searchString);

	Autocomplete searchSuggest(String searchString);

	void addFilter(ClientRequestFilter filter);

	IdentificationResult identifyRecipient(Identification identification);
}
