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

import org.junit.jupiter.api.Test;

import static no.digipost.api.client.representations.Relation.ADD_CONTENT;
import static no.digipost.api.client.representations.Relation.SELF;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LinkTest {

    private static final String RELATIONS_URI = "https://api.digipost.no/relations";
    private static final String ADD_CONTENTS_REL_URI = RELATIONS_URI + "/" + ADD_CONTENT.name().toLowerCase();

    @Test
    public void shouldParseRelationFromUri() {
        Link link = new Link();
        link.setRel(RELATIONS_URI + "/" + SELF.name());
        assertThat(link.getRelationName(), is(SELF));
    }

    @Test
    public void shouldBuildRelationUri() {
        Link link = new Link(ADD_CONTENT, new DigipostUri(RELATIONS_URI + "/" + ADD_CONTENT));
        assertThat(link.getRel(), is(ADD_CONTENTS_REL_URI));
    }

    @Test
    public void shouldIndicateLinkRelationEquality() {
        Link link = new Link(ADD_CONTENT, new DigipostUri(RELATIONS_URI + "/" + ADD_CONTENT));
        assertThat(link.equalsRelation(ADD_CONTENT), is(true));
    }

    @Test
    public void shouldIndicateLinkRelationEquality2() {
        Link link = new Link();
        link.setRel(RELATIONS_URI + "/" + SELF.name());
        assertThat(link.equalsRelation(SELF), is(true));
    }
}
