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
package no.digipost.api.client.representations;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public Recipients createRecipients() {
        return new Recipients();
    }

    public ErrorMessage createErrorMessage() {
        return new ErrorMessage();
    }

    public Autocomplete createAutocomplete() {
        return new Autocomplete();
    }

    public Message createMessage() {
        return new Message();
    }

    public MessageDelivery createMessageDelivery() {
        return new MessageDelivery();
    }

    public EntryPoint createEntryPoint() {
        return new EntryPoint();
    }

    public Invoice createInvoice() {
        return new Invoice();
    }

    public DocumentEvents createDocumentEvents() {
        return new DocumentEvents();
    }

    public DocumentStatus createDocumentStatus() {
        return new DocumentStatus();
    }
}
