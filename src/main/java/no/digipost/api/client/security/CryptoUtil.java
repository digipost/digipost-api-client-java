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

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Enumeration;

public final class CryptoUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CryptoUtil.class);

    public static PrivateKey loadKeyFromP12(final InputStream certificateStream, final String passord) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(certificateStream, passord.toCharArray());
            final Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                LOG.debug("Trying to get private key for alias: " + alias);
                if (keyStore.isKeyEntry(alias)) {
                    RSAPrivateCrtKey key = (RSAPrivateCrtKey) keyStore.getKey(alias, passord.toCharArray());
                    if (key != null) {
                        LOG.debug("Found private key for alias: " + alias);
                        return key;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading private key", e);
        }
        throw new RuntimeException("No private key found in certificate file");
    }

    public static byte[] sign(final PrivateKey privateKey, final String messageToSign) {
        Signature instance;
        try {
            instance = Signature.getInstance("SHA256WithRSAEncryption");
            instance.initSign(privateKey);
            instance.update(messageToSign.getBytes());
            return instance.sign();
        } catch (Exception e) {
            throw new RuntimeException("Det skjedde en feil ved signeringen", e);
        }
    }

    public static void addBouncyCastleProviderAndVerify_AES256_CBC_Support() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            LOG.debug("Registered BouncyCastleProvider");
            new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
            LOG.debug("Support for AES256_CBC ok");
        } catch (CMSException e) {
            throw new RuntimeException("Feil under initialisering av algoritmer. Er Java Cryptographic Excetsions (JCE) installert?", e);
        }
    }

    private CryptoUtil() {
    }
}
