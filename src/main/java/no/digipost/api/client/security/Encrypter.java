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
package no.digipost.api.client.security;

import no.digipost.api.client.errorhandling.DigipostClientException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static no.digipost.api.client.errorhandling.ErrorCode.ENCRYPTION_KEY_NOT_FOUND;
import static no.digipost.api.client.errorhandling.ErrorCode.FAILED_PREENCRYPTION;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;

public final class Encrypter {

    /**
     * Encrypter with no key, i.e. it will throw an appropriate exception if trying
     * to encrypt anything with it.
     */
    public static final Encrypter FAIL_IF_TRYING_TO_ENCRYPT = new Encrypter();

    private final JceKeyTransRecipientInfoGenerator keyInfoGenerator;
    private static final JceCMSContentEncryptorBuilder encryptorBuilder = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME);

    public static Encrypter using(DigipostPublicKey key) {
        return new Encrypter(new JceKeyTransRecipientInfoGenerator(key.publicKeyHash.getBytes(), key.publicKey));
    }

    public static Encrypter using(X509Certificate certificate) {
        try {
            return new Encrypter(new JceKeyTransRecipientInfoGenerator(certificate));
        } catch (CertificateEncodingException e) {
            throw new DigipostClientException(FAILED_PREENCRYPTION, "Feil ved kryptering av innhold: " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
        }
    }

    private Encrypter(JceKeyTransRecipientInfoGenerator keyInfoGenerator) {
        this.keyInfoGenerator = keyInfoGenerator;
    }

    private Encrypter() {
        keyInfoGenerator = null;
    }

    public InputStream encrypt(InputStream content) {
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getClass().getSimpleName() + ": '" + e.getMessage() + "'", e);
        }
        return encrypt(bytes);
    }

    public InputStream encrypt(byte[] content) {
        if (keyInfoGenerator == null) {
            throw new DigipostClientException(ENCRYPTION_KEY_NOT_FOUND, "Trying to preencrypt but have no encryption key.");
        }

        try {
            CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
            gen.addRecipientInfoGenerator(keyInfoGenerator);

            CMSEnvelopedData d = gen.generate(new CMSProcessableByteArray(content), encryptorBuilder.build());
            return new ByteArrayInputStream(d.getEncoded());
        } catch (Exception e) {
            if (e instanceof CMSException && getRootCause(e) instanceof InvalidKeyException) {
                throw new DigipostClientException(FAILED_PREENCRYPTION,
                        "Ugyldig krypteringsnøkkel. (" + InvalidKeyException.class.getName() + ") Er Java Cryptographic Extensions (JCE) " +
                        "Unlimited Strength Jurisdiction Policy Files installert? " +
                        "Dette kan lastes ned fra http://www.oracle.com/technetwork/java/javase/downloads/ under \"Additional Resources\". " +
                        "Plasser filene US_export_policy.jar og local_policy.jar i ${JAVA_HOME}/jre/lib/security (overskriv eksisterende).", e);
            } else {
                throw new DigipostClientException(FAILED_PREENCRYPTION, "Feil ved kryptering av innhold: " + e.getClass().getSimpleName() + " '" + e.getMessage() + "'", e);
            }
        }
    }
}
