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
package no.digipost.api.client.representations.inbox;

import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Relation;
import no.digipost.api.client.representations.Representation;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "letter", propOrder = {
        "subject",
        "sender",
        "date",
        "read",
        "attachments",
        "links"
})
public class Letter extends Representation {

    @XmlElement(name = "subject")
    public String subject;
    @XmlElement(name = "sender")
    public String sender;
    @XmlElement(name = "date")
    public DateTime date;
    @XmlElement(name = "read")
    public Boolean read;

    @XmlElement(name = "attachment")
    public List<Letter> attachments = new ArrayList<>();

    @XmlElement(name = "link")
    protected List<Link> getLinks() {
        return links;
    }

    protected void setLinks(final List<Link> links) {
        this.links = links;
    }

    public Letter(String subject, String sender, DateTime date, Boolean read, List<Letter> attachments, List<Link> links) {
        this.subject = subject;
        this.sender = sender;
        this.date = date;
        this.read = read;
        this.attachments = attachments;
        this.links = links;
    }

    public Letter() {
    }

    public URI getContentLink() {
        return getLinkByRelationName(Relation.GET_INBOX_LETTER_CONTENT).getUri();
    }

    public URI getDeleteLink() { return getLinkByRelationName(Relation.DELETE_INBOX_LETTER).getUri(); }



}
