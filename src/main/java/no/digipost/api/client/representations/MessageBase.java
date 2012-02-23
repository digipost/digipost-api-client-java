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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "message-base", propOrder = { "messageId", "senderId", "preEncrypt" })
public abstract class MessageBase extends Representation {

	@XmlElement(name = "message-id", required = true)
	protected String messageId;
	@XmlElement(name = "sender-id")
	protected Long senderId;
	@XmlElement(name = "pre-encrypt")
	protected Boolean preEncrypt;

	protected MessageBase() {
	}

	public MessageBase(final String messageId, final Link... links) {
		super(links);
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(final String messageId) {
		this.messageId = messageId;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(final Long senderId) {
		this.senderId = senderId;
	}

	public void setPreEncrypt(final Boolean preEncrypt) {
		this.preEncrypt = preEncrypt;
	}

	public boolean skalPrekrypteres() {
		return preEncrypt != null && preEncrypt;
	}

	public Link getSelfLink() {
		return getLinkByRelationName(Relation.SELF);
	}

	public Link getAddContentAndSendLink() {
		return getLinkByRelationName(Relation.ADD_CONTENT_AND_SEND);
	}

	public Link getEncryptionKeyLink() {
		return getLinkByRelationName(Relation.GET_ENCRYPTION_KEY);
	}

	// TODO: hvor streng skal denne være? - sjekke alle felter
	public abstract boolean isSameMessageAs(final Object message);

}