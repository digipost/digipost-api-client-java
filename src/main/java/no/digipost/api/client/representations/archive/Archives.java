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
package no.digipost.api.client.representations.archive;

import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "archives", propOrder = {
        "archives",
        "links"
})
@XmlRootElement(name = "archives")
public class Archives extends Representation {

    @XmlElement(name = "archive", nillable = false)
    protected List<Archive> archives;

    @XmlElement(name = "links")
    protected List<Link> getLinks() {
        return links;
    }

    public Archives() {
        super();
    }

    public Archives(final List<Archive> archives, final List<Link> links) {
        this.archives = archives;
        this.links = links;
    }

    public List<Archive> getArchives() {
        if (archives == null) {
            archives = new ArrayList<>();
        }
        return this.archives;
    }
    public Optional<Archive> findDefault(){
        return this.getArchives().stream().filter(a -> a.name == null).findAny();
    }
}
