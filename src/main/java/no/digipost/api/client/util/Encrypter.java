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
import no.motif.Exceptions;
import no.motif.f.Fn;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static no.digipost.api.client.errorhandling.ErrorCode.ENCRYPTION_KEY_NOT_FOUND;
import static no.digipost.api.client.errorhandling.ErrorCode.FAILED_PREENCRYPTION;

public final class Encrypter {

	/**
	 * Encrypter with no key, i.e. it will throw an appropriate exception if trying
	 * to encrypt anything with it.
	 */
	public static final Encrypter FAIL_IF_TRYING_TO_ENCRYPT = new Encrypter(null);

	public static final Fn<DigipostPublicKey, Encrypter> keyToEncrypter = new Fn<DigipostPublicKey, Encrypter>() {
		@Override
        public Encrypter $(DigipostPublicKey key) {
			return Encrypter.using(key);
        }
	};


	public static Encrypter using(DigipostPublicKey digipostPublicKey) {
		return new Encrypter(digipostPublicKey);
	}





	private final DigipostPublicKey key;
	private final JceCMSContentEncryptorBuilder encryptorBuilder;

	private Encrypter(DigipostPublicKey key) {
		this.key = key;
		this.encryptorBuilder = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME);
	}


	public InputStream encrypt(InputStream content) {
		byte[] bytes;
        try {
	        bytes = IOUtils.toByteArray(content);
        } catch (IOException e) {
	        throw Exceptions.asRuntimeException(e);
        }
		return encrypt(bytes);
	}

	public InputStream encrypt(byte[] content) {
		if (key == null) {
			throw new DigipostClientException(ENCRYPTION_KEY_NOT_FOUND, "Trying to preencrypt but have no encryption key.");
		}
		try {
			CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
			gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(key.publicKeyHash.getBytes(), key.publicKey));
			CMSEnvelopedData d = gen.generate(new CMSProcessableByteArray(content), encryptorBuilder.build());
			return new ByteArrayInputStream(d.getEncoded());
		} catch (Exception e) {
			throw new DigipostClientException(FAILED_PREENCRYPTION, "Feil ved kryptering av innhold: " + e.getMessage(), e);
		}
	}



}
