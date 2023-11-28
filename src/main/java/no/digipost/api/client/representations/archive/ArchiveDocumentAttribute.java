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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archive-document-attribute", propOrder = {
        "key",
        "value"
})
@XmlRootElement(name = "archive-document-attribute")
public class ArchiveDocumentAttribute {

    @XmlElement(name = "key", required = true)
    protected String key;
    @XmlElement(name = "value", required = true)
    protected String value;

    public ArchiveDocumentAttribute() {
        super();
    }

    public ArchiveDocumentAttribute(final String key, final String uri) {
        this.key = key;
        this.value = uri;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{key='" + key + "', value='" + value + "'}";
    }
}
