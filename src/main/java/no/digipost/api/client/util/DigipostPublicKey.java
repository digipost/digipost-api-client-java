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
package no.digipost.api.client.util;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.EncryptionKey;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class DigipostPublicKey {
	private static final Logger LOG = LoggerFactory.getLogger(DigipostPublicKey.class);
	public final PublicKey publicKey;
	public final String publicKeyHash;

	public DigipostPublicKey(EncryptionKey encryptionKey) {
		try {
			PEMParser pemParser = new PEMParser(new StringReader(encryptionKey.getValue()));
			SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
			X509EncodedKeySpec spec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
			IOUtils.closeQuietly(pemParser);
			PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

			this.publicKey = publicKey;
			this.publicKeyHash = encryptionKey.getKeyId();

		} catch (Exception e) {
			LOG.error("Feil ved parsing av krypteringsnøkkel fra Digipost.", e);
			throw new DigipostClientException(ErrorCode.FAILED_TO_PARSE_ENCRYPTION_KEY, "Feil ved parsing av krypteringsnøkkel fra Digipost.");
		}

	}
}
