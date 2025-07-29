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
package no.digipost.api.client.security;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.EncryptionKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;

import java.io.Reader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class DigipostPublicKey {
    public final PublicKey publicKey;
    public final String publicKeyHash;

    public DigipostPublicKey(EncryptionKey encryptionKey) {

        try (Reader sourceReader = new StringReader(encryptionKey.getValue());
             PEMParser pemParser = new PEMParser(sourceReader)) {

            SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

            this.publicKey = publicKey;
            this.publicKeyHash = encryptionKey.getKeyId();

        } catch (Exception e) {
            throw new DigipostClientException(ErrorCode.FAILED_TO_PARSE_ENCRYPTION_KEY, "Feil ved parsing av krypteringsnøkkel fra Digipost.", e);
        }

    }
}
