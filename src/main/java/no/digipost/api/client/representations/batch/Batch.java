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
package no.digipost.api.client.representations.batch;


import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Representation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.net.URI;
import java.util.List;

import static java.util.Optional.ofNullable;
import static no.digipost.api.client.representations.Relation.COMPLETE_BATCH;
import static no.digipost.api.client.representations.Relation.SELF_DELETE;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "batch", propOrder = {
        "uuid",
        "status",
        "countDigipost",
        "countPrint",
        "links"
})
@XmlRootElement(name = "batch")
public class Batch extends Representation {

    @XmlElement(required = true)
    protected String uuid;
    @XmlSchemaType(name = "string")
    protected BatchStatus status;
    @XmlElement(name = "count-digipost")
    protected Integer countDigipost;
    @XmlElement(name = "count-print")
    protected Integer countPrint;

    public Batch() {
        super();
    }

    public Batch(String uuid) {
        super();
        this.uuid = uuid;
    }

    public Batch(String uuid, BatchStatus status, Integer countDigipost, Integer countPrint, final List<Link> links) {
        this.uuid = uuid;
        this.status = status;
        this.countDigipost = countDigipost;
        this.countPrint = countPrint;
        super.links = links;
    }

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        return this.links;
    }

    protected void setLink(final List<Link> links) {
        this.links = links;
    }

    public URI getCompleteBatch() {
        return getLinkByRelationName(COMPLETE_BATCH).getUri();
    }

    public URI getCancelBatch() {
        return getLinkByRelationName(SELF_DELETE).getUri();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " with uuid '" + uuid + "'" +
                ofNullable(status).map(t -> ", status '" + t + "'").orElse("") +
                ofNullable(countDigipost).map(t -> ", Digipost: '" + t + "'").orElse("") +
                ofNullable(countPrint).map(t -> ", Print: '" + t + "'").orElse("");
    }

	public String getUuid() {
		return uuid;
	}

	public BatchStatus getStatus() {
		return status;
	}

	public Integer getCountDigipost() {
		return countDigipost;
	}

	public Integer getCountPrint() {
		return countPrint;
	}
}
