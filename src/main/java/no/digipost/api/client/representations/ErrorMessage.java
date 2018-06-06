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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static java.lang.String.format;
import static no.digipost.api.client.errorhandling.ErrorCode.GENERAL_ERROR;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "error")
public class ErrorMessage extends Representation {

    public static final ErrorMessage EMPTY = new ErrorMessage(ErrorType.NONE, "Empty error-message");
    @XmlElement(name = "error-code", required = true)
    private String errorCode;

    @XmlElement(name = "error-message", required = true)
    private String errorMessage;

    @XmlElement(name = "error-type", required = true)
    private ErrorType errorType;


    public ErrorMessage(ErrorType errorType, String errorMessage, Link... linker) {
        this(errorType, GENERAL_ERROR.name(), errorMessage, linker);
    }

    public ErrorMessage(ErrorType errorType, String errorCode, String errorMessage, Link... linker) {
        super(linker);
        this.errorType = errorType;
        this.errorCode = errorCode;
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
        return format("%s: %s", errorCode, errorMessage);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @XmlElement(name = "link")
    public List<Link> getLink() {
        return links;
    }

    public void setLink(final List<Link> links) {
        this.links = links;
    }

    public ErrorMessage withMessage(String otherErrorMessage) {
        return new ErrorMessage(errorType, errorCode, otherErrorMessage, links.toArray(new Link[0]));
    }
}
