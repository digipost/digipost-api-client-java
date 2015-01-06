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
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.*;
import no.digipost.api.client.util.Encrypter;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Implementasjon av {@link SendableDelivery#send() selve sendingen} av
 * en forsendelse, som en multipart request.
 */
class MultipartSendMessage implements SendableDelivery {

	private final MessageSender sender;
    private final Message message;
    private final Map<Document, InputStream> documents;
	private final ApiService apiService;


	MultipartSendMessage(Message message, ApiService apiService, EventLogger eventLogger) {
		this.apiService = apiService;
		this.sender = new MessageSender(apiService, eventLogger);
		this.message = message;
		this.documents = new HashMap<>();
    }

    public final void add(Document document, InputStream content) {
    	documents.put(document, content);
    }

    @Override
    public final MessageDelivery send() {
		EncryptionKey krypteringsnokkel = fetchEncryptionKeyForRecipientIfNecessary();

	    try (MultiPart multiPart = new MultiPart()) {
	    	BodyPart messageBodyPart = new BodyPart(message, MediaType.valueOf(MediaTypes.DIGIPOST_MEDIA_TYPE_V6));
	    	ContentDisposition messagePart = ContentDisposition.type("attachment").fileName("message").build();
	    	messageBodyPart.setContentDisposition(messagePart);
	    	multiPart.bodyPart(messageBodyPart);

	    	for (Entry<Document, InputStream> document : documents.entrySet()) {
	    		Document metadata = document.getKey();
	    		InputStream content = document.getValue();
				if (metadata.isPreEncrypt()) {
					if (krypteringsnokkel == null) throw new IllegalStateException("Trying to preencrypt but have no encryption key.");
					content = Encrypter.encryptContent(content, krypteringsnokkel);
				}
	    		BodyPart bodyPart = new BodyPart(content, new MediaType("application", defaultIfBlank(metadata.getDigipostFileType(), "octet-stream")));
	    		ContentDisposition documentPart = ContentDisposition.type("attachment").fileName(metadata.uuid).build();
	    		bodyPart.setContentDisposition(documentPart);
	    		multiPart.bodyPart(bodyPart);
	    	}
    		return sender.createMultipartMessage(multiPart);

	    } catch (DigipostClientException e) {
	    	throw e;
	    } catch (Exception e) {
	    	throw new DigipostClientException(ErrorCode.resolve(e), e);
        }
    }

	private EncryptionKey fetchEncryptionKeyForRecipientIfNecessary() {
		boolean someDocumentShouldBePreencrypted = false;
		for (Document document : documents.keySet()) {
			if (document.isPreEncrypt()) {
				someDocumentShouldBePreencrypted = true;
				break;
			}
		}

		EncryptionKey krypteringsnokkel = null;
		if (someDocumentShouldBePreencrypted) {
			krypteringsnokkel = sender.getRecipientEncryptionKey(message.recipient);
		}
		return krypteringsnokkel;
	}
}
