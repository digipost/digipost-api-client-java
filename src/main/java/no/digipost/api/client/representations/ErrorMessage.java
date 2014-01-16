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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "error")
public class ErrorMessage extends Representation {

	@XmlElement(name = "error-message", required = true)
	private String errorMessage;

	public ErrorMessage(final String errorMessage, final Link... linker) {
		super(linker);
		this.errorMessage = errorMessage;
	}

	ErrorMessage() {
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(errorMessage).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ErrorMessage other = (ErrorMessage) obj;
		return new EqualsBuilder().append(errorMessage, other.errorMessage).isEquals();
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(), errorMessage);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@XmlElement(name = "link")
	public List<Link> getLink() {
		return links;
	}

	public void setLink(final List<Link> links) {
		this.links = links;
	}
}
