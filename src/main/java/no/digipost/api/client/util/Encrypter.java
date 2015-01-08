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
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Encrypter {

	private static OutputEncryptor buildEncryptor() throws CMSException {
		return new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
	}

	public static InputStream encryptContent(InputStream content, DigipostPublicKey key) {
		try {
			CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
			gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(key.publicKeyHash.getBytes(), key.publicKey));
			CMSEnvelopedData d = gen.generate(new CMSProcessableByteArray(IOUtils.toByteArray(content)), buildEncryptor());
			return new ByteArrayInputStream(d.getEncoded());

		} catch (Exception e) {
			throw new DigipostClientException(ErrorCode.FAILED_PREENCRYPTION, "Feil ved kryptering av innhold.", e);
		}
	}

}
