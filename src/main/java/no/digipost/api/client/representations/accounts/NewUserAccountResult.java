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

@XmlRootElement(name = "new-user-account-result")
public class NewUserAccountResult {

	public enum ResultCode {
		OK_CREATED,
		OK_REACTIVATED,
		OK_ALLREADY_ACTIVE,
		ERROR_UNABLE_TO_CREATE;
	}

	@XmlElement(name = "digipost-address")
	private final DigipostAddress digipostAddress;
    @XmlElement(name = "encryption-key")
	private final EncryptionKey encryptionKey;
    @XmlElement(name = "result-code")
	private final String resultCode;
    @XmlElement(name = "result-description")
	private final String resultDescription;

	public NewUserAccountResult(DigipostAddress digipostAddress, EncryptionKey encryptionKey, String resultCode, String resultDescription) {
		this.digipostAddress = digipostAddress;
		this.encryptionKey = encryptionKey;
		this.resultCode = resultCode;
		this.resultDescription = resultDescription;
	}

	private NewUserAccountResult() {
	    this(null, null, null, null);
    }

	public DigipostAddress getDigipostAddress() {
		return digipostAddress;
	}

	public EncryptionKey getEncryptionKey() {
		return encryptionKey;
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getResultDescription() {
		return resultDescription;
	}

    @Override
    public String toString() {
        return "NewUserAccountResult{" +
                "digipostAddress=" + digipostAddress +
                ", encryptionKey=" + encryptionKey +
                ", resultCode=" + resultCode +
                ", resultDescription='" + resultDescription + '\'' +
                '}';
    }
}
