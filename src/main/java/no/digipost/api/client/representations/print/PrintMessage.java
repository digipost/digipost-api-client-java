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
package no.digipost.api.client.representations.print;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MessageBase;

import org.apache.commons.lang.NotImplementedException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "print-message", propOrder = { "recipient", "returnAddress", "postType", "eaCode", "status", "links" })
@XmlRootElement(name = "print-message")
public class PrintMessage extends MessageBase {

	public PrintMessage() {
	}

	@XmlElement(required = true)
	protected PrintRecipient recipient;
	@XmlElement(name = "return-address", required = true)
	protected PrintRecipient returnAddress;
	@XmlElement(name = "post-type", required = true)
	protected String postType;
	@XmlElement(name = "ea-code")
	protected String eaCode;
	protected PrintMessageStatus status;
	@XmlElement(name = "link")
	protected List<Link> links;

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

	public String getEaCode() {
		return eaCode;
	}

	public void setEaCode(final String value) {
		eaCode = value;
	}

	public PrintMessageStatus getStatus() {
		return status;
	}

	public void setStatus(final PrintMessageStatus value) {
		status = value;
	}

	public List<Link> getLinks() {
		if (links == null) {
			links = new ArrayList<Link>();
		}
		return links;
	}

	public void setLinks(final List<Link> value) {
		links = null;
		List<Link> draftl = getLinks();
		draftl.addAll(value);
	}

	@Override
	public boolean isIdenticalTo(final Object message) {
		if (!(message instanceof PrintMessage))
			return false;
		throw new NotImplementedException();
	}

}
