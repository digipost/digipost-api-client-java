/*
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
package no.digipost.api.client.representations.archive;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive-document-content", propOrder = {
        "contentType",
        "uri"
})
@XmlRootElement(name = "archive-document-content")
public class ArchiveDocumentContent {

    @XmlElement(name = "content-type", required = true)
    protected String contentType;
    @XmlElement(required = true)
    protected String uri;

    public ArchiveDocumentContent() {
        super();
    }

    public ArchiveDocumentContent(final String contentType, final String uri) {
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
        return "ArchiveDocumentContent{" +
                "contentType='" + contentType + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
