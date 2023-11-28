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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "peppol-addresses", propOrder = {
        "receiver",
        "sender",
})
public class PeppolAddresses {
    @XmlElement(name = "receiver", required = true)
    protected PeppolAddress receiver;
    @XmlElement(name = "sender", required = true)
    protected PeppolAddress sender;

    PeppolAddresses() {
    }

    public PeppolAddresses(String receiver, String sender) {
        this.receiver = new PeppolAddress(receiver);
        this.sender = new PeppolAddress(sender);
    }
    
    public PeppolAddresses(PeppolAddress receiver, PeppolAddress sender) {
        this.receiver = receiver;
        this.sender = sender;
    }

    public PeppolAddress getReceiver() {
        return receiver;
    }

    public PeppolAddress getSender() {
        return sender;
    }
}
