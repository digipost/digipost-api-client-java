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
package no.digipost.api.client.representations.accounts;

import no.digipost.api.client.representations.DigipostAddress;
import no.digipost.api.client.representations.EncryptionKey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user-account")
public class UserAccount {

	@XmlElement(name = "digipost-address")
	private final DigipostAddress digipostAddress;
    @XmlElement(name = "encryption-key")
	private final EncryptionKey encryptionKey;

	public UserAccount(DigipostAddress digipostAddress, EncryptionKey encryptionKey, String resultCode, String resultDescription) {
		this.digipostAddress = digipostAddress;
		this.encryptionKey = encryptionKey;
	}

	private UserAccount() {
	    this(null, null, null, null);
    }

	public DigipostAddress getDigipostAddress() {
		return digipostAddress;
	}

	public EncryptionKey getEncryptionKey() {
		return encryptionKey;
	}

    @Override
    public String toString() {
        return "UserAccount{" +
                "digipostAddress=" + digipostAddress +
                ", encryptionKey=" + encryptionKey +
                '}';
    }
}
