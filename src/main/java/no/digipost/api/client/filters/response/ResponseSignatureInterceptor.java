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
package no.digipost.api.client.filters.response;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.representations.EntryPoint;
import no.digipost.api.client.security.ClientResponseToVerify;
import no.digipost.api.client.security.ResponseMessageSignatureUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.function.Supplier;

import static no.digipost.api.client.Headers.X_Digipost_Signature;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseSignatureInterceptor implements HttpResponseInterceptor {

    public static final String NOT_SIGNED_RESPONSE = "NOT_SIGNED_RESPONSE";
    private final Supplier<EntryPoint> entryPointResolver;

    public ResponseSignatureInterceptor(final Supplier<EntryPoint> entryPointResolver) {
        this.entryPointResolver = entryPointResolver;
    }

    @Override
    public void process(HttpResponse response, HttpContext context) {
        final Boolean notSignedResponse = (Boolean) context.getAttribute(NOT_SIGNED_RESPONSE);
        if (notSignedResponse != null && notSignedResponse) {
            return;
        }

        try {
            String serverSignaturBase64 = getServerSignaturFromResponse(response);
            byte[] serverSignaturBytes = Base64.decode(serverSignaturBase64.getBytes());

            String signatureString = ResponseMessageSignatureUtil.getCanonicalResponseRepresentation(new ClientResponseToVerify(context, response));

            Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
            instance.initVerify(lastSertifikat());
            instance.update(signatureString.getBytes());
            boolean verified = instance.verify(serverSignaturBytes);
            if (!verified) {
                throw new DigipostClientException(SERVER_SIGNATURE_ERROR, "Response from server did not match signature.");
            }
        } catch (Exception e) {
            if (e instanceof DigipostClientException) {
                throw (DigipostClientException) e;
            } else {
                throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                        "An exception occured during server response signature verification. "
                                + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }
    }


    private String getServerSignaturFromResponse(final HttpResponse response) {
        String serverSignaturString = null;
        Header firstHeader = response.getFirstHeader(X_Digipost_Signature);
        if(firstHeader != null){
            serverSignaturString = firstHeader.getValue();
        }

        if (isBlank(serverSignaturString)) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                    "Missing " + X_Digipost_Signature + " header. Signature from server could not be validated");
        }
        return serverSignaturString;
    }

    public X509Certificate lastSertifikat() {
        try {
            InputStream certStream = new ByteArrayInputStream(entryPointResolver.get().getCertificate().getBytes());

            CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
            X509Certificate sertifikat = (X509Certificate) cf.generateCertificate(certStream);
            if (sertifikat == null) {
                throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                        "Unable to load Digipost's public key. Signature from server could not be validated");
            }
            return sertifikat;
        } catch (GeneralSecurityException e) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                    "Unable to load Digipost's public key. Signature from server could not be validated");
        }
    }
}
