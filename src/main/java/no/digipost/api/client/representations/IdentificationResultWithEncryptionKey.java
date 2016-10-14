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
package no.digipost.api.client.representations;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identification-result-with-encryption-key", propOrder = {
		"result",
		"encryptionKey"
})
@XmlRootElement(name = "identification-result-with-encryption-key")
public class IdentificationResultWithEncryptionKey {

	@XmlElement(name="identification-result", required = true)
	protected IdentificationResult result;
	@XmlElement(name = "encryption-key")
	protected EncryptionKey encryptionKey;

	public IdentificationResultWithEncryptionKey() {
		this(null, null);
	}

	public IdentificationResultWithEncryptionKey(IdentificationResult result, EncryptionKey encryptionKey) {
		this.result = result;
		this.encryptionKey = encryptionKey;
	}

	public IdentificationResult getResult() {
		return result;
	}

	public IdentificationResultCode getResultCode(){
		return result.getResult();
	}

	public EncryptionKey getEncryptionKey() {
		return encryptionKey;
	}

	@Override
	public String toString() {
		return "IdentificationResultWithEncryptionKey{" +
				"result=" + result.toString() +
				", encryption-key=" + encryptionKey.toString() +
				'}';
	}
}
