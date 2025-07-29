/*
 * Copyright (C) Posten Bring AS
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
package no.digipost.api.client.representations.shareddocuments;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "shared-document-content", propOrder = {
        "contentType",
        "uri"
})
@XmlRootElement(name = "shared-document-content")
public class SharedDocumentContent {

    @XmlElement(name = "content-type", required = true)
    protected String contentType;
    @XmlElement(required = true)
    protected String uri;

    public SharedDocumentContent() {
        super();
    }

    public SharedDocumentContent(final String contentType, final String uri) {
        this.contentType = contentType;
        this.uri = uri;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "SharedDocumentContent{" +
                "contentType='" + contentType + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
