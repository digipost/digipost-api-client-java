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
package no.digipost.api.client.representations.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.net.URI;

public class URIXmlAdapter extends XmlAdapter<String, URI> {

    @Override
    public URI unmarshal(final String value) {
        return URI.create(value);
    }

    @Override
    public String marshal(final URI uri) {
        return uri.toString();
    }
}
