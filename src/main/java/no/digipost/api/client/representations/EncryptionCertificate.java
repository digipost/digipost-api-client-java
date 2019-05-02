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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.io.ByteArrayInputStream;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "encryption-certificate", propOrder = {
        "value"
})
@XmlRootElement(name = "encryption-certificate")
public class EncryptionCertificate extends Representation {

    public EncryptionCertificate() {
    }

    public EncryptionCertificate(String value) {
        this.value = value;
    }

    @XmlValue
    protected String value;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EncryptionCertificate{" +
                "value='" + value + '\'' +
                '}';
    }

    public X509Certificate getX509Certificate() {

        Certificate certificate;
        CertificateFactory certificateFactory;

        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException(
                    "Could not create X.509 certificate factory: '" + e.getMessage() + "'. " +
                            "Available providers: " + Stream.of(Security.getProviders()).map(Provider::getName).collect(joining(", ")), e);
        }

        try {
            certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(getValue().getBytes()));
        } catch (CertificateException e) {
            throw new RuntimeException("Unable to generate certificate from value " + getValue() + ". Message was: " + e.getMessage(), e);
        }

        if (certificate instanceof X509Certificate) {
            return (X509Certificate) certificate;
        } else {
            throw new RuntimeException("Not a X.509 certificate. The given certificate of type " + certificate.getType() + " can not be used.");
        }
    }
}
