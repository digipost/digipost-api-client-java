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
package no.digipost.api.client.delivery;

import no.digipost.api.client.ApiService;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.MessageSender;
import no.digipost.api.client.delivery.OngoingDelivery.SendableDelivery;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.MediaTypes;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageDelivery;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementasjon av {@link SendableDelivery#send() selve sendingen} av
 * en forsendelse, som en multipart request.
 */
class MultipartSendMessage implements SendableDelivery {

	private final MessageSender sender;
    private final Message message;
    private final Map<Document, InputStream> documents;


    MultipartSendMessage(Message message, ApiService apiService, EventLogger eventLogger) {
		this.sender = new MessageSender(apiService, eventLogger);
		this.message = message;
		this.documents = new HashMap<>();
    }

    public final void add(Document document, InputStream content) {
    	documents.put(document, content);
    }


    @Override
    public final MessageDelivery send() {
	    FormDataMultiPart formData = new FormDataMultiPart();
		formData.field("message", message, MediaType.valueOf(MediaTypes.DIGIPOST_MEDIA_TYPE_V5));

		for (Entry<Document, InputStream> doc : documents.entrySet()) {
			Document metadata = doc.getKey();
			InputStream content = doc.getValue();
			formData.field(metadata.uuid, content, new MediaType("application", metadata.getDigipostFileType()));
		}

		return sender.createMultipartMessage(formData);
    }
}
