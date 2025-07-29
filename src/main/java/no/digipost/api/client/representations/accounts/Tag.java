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
package no.digipost.api.client.representations.accounts;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tag", propOrder = { "publicMailboxTag" })
@XmlRootElement(name = "tag")
public class Tag {

    @XmlElement(name = "public-mailbox-tag")
    protected PublicMailboxTag publicMailboxTag;

    public Tag() {
    }

    public Tag(PublicMailboxTag publicMailboxTag) {
        this.publicMailboxTag = publicMailboxTag;
    }

    public PublicMailboxTag getPublicMailboxTag() {
        return publicMailboxTag;
    }
}
