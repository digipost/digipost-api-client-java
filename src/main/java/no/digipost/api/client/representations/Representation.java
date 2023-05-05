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
package no.digipost.api.client.representations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class Representation {

    @XmlTransient
    protected List<Link> links;

    public Representation(final Link... linker) {
        links = new ArrayList<>(Arrays.asList(linker));
    }

    Representation() {
        links = new ArrayList<>();
    }

    public void addLink(final Link link) {
        links.add(link);
    }

    protected Link getLinkByRelationName(final Relation relation) {
        for (Link l : links) {
            if (l.equalsRelation(relation)) {
                return l;
            }
        }
        return null;
    }
}
