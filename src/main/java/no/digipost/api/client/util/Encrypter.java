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

import no.digipost.api.client.representations.EncryptionKey;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OutputEncryptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Encrypter {

	private static OutputEncryptor buildEncryptor() throws CMSException {
		return new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
	}

	public static InputStream encryptContent(InputStream content, EncryptionKey key) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(key.getValue()));
		SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
		X509EncodedKeySpec spec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
		IOUtils.closeQuietly(pemParser);
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

		CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
		gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(key.getKeyId().getBytes(), publicKey));
		CMSEnvelopedData d = gen.generate(new CMSProcessableByteArray(IOUtils.toByteArray(content)), buildEncryptor());
		return new ByteArrayInputStream(d.getEncoded());
	}

}
