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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "print-message", propOrder = { "recipient", "returnAddress", "postType", "status", "links" })
@XmlRootElement(name = "print-message")
public class PrintMessage extends MessageBase {

	public PrintMessage() {
	}

	// TODO: fjerne linker fra konstruktøren? I tilfelle gjør det samme i
	// MessageBase og Message
	public PrintMessage(final String messageId, final PrintRecipient recipient, final PrintRecipient returnAddress, final String postType,
			final Link... links) {
		super(messageId, links);
		this.recipient = recipient;
		this.returnAddress = returnAddress;
		this.postType = postType;

	}

	@XmlElement(required = true)
	protected PrintRecipient recipient;
	@XmlElement(name = "return-address", required = true)
	protected PrintRecipient returnAddress;
	@XmlElement(name = "post-type", required = true)
	protected String postType;
	protected PrintMessageStatus status;

	public PrintRecipient getRecipient() {
		return recipient;
	}

	public void setRecipient(final PrintRecipient value) {
		recipient = value;
	}

	public PrintRecipient getReturnAddress() {
		return returnAddress;
	}

	public void setReturnAddress(final PrintRecipient value) {
		returnAddress = value;
	}

	public String getPostType() {
		return postType;
	}

	public void setPostType(final String value) {
		postType = value;
	}

	public PrintMessageStatus getStatus() {
		return status;
	}

	public void setStatus(final PrintMessageStatus value) {
		status = value;
	}

	@XmlElement(name = "link")
	protected List<Link> getLinks() {
		return links;
	}

	protected void setLinks(final List<Link> links) {
		this.links = links;
	}

	@Override
	public boolean isSameMessageAs(final Object message) {
		if (!(message instanceof PrintMessage)) {
			return false;
		}

		PrintMessage other = (PrintMessage) message;

		return messageId.equals(other.messageId) && recipient.isSameRecipientAs(other.recipient);
	}

}
