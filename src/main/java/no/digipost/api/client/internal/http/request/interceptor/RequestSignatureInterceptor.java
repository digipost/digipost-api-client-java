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
package no.digipost.api.client.internal.http.request.interceptor;

import no.digipost.api.client.EventLogger;
import no.digipost.api.client.internal.http.Headers;
import no.digipost.api.client.security.RequestMessageSignatureUtil;
import no.digipost.api.client.security.Signer;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestSignatureInterceptor implements HttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestSignatureInterceptor.class);

    private final Signer signer;
    private final EventLogger eventLogger;

    public RequestSignatureInterceptor(Signer signer) {
        this(signer, EventLogger.NOOP_LOGGER);
    }

    public RequestSignatureInterceptor(Signer signer, EventLogger eventLogger) {
        this.eventLogger = (eventLogger != null ? eventLogger : EventLogger.NOOP_LOGGER).withDebugLogTo(LOG);
        this.signer = signer;
    }

    private void setSignatureHeader(HttpRequest httpRequest) {
        String stringToSign = RequestMessageSignatureUtil.getCanonicalRequestRepresentation(new ApacheHttpRequestToSign(httpRequest));
        eventLogger.log(getClass().getSimpleName() + " beregnet streng som skal signeres:\n" +
                        "===START SIGNATURSTRENG===\n" +
                        stringToSign +
                        "===SLUTT SIGNATURSTRENG===");

        byte[] signatureBytes = signer.sign(stringToSign);
        String signature = new String(Base64.encode(signatureBytes));
        httpRequest.setHeader(Headers.X_Digipost_Signature, signature);
        eventLogger.log(getClass().getSimpleName() + " satt headeren " + Headers.X_Digipost_Signature + "=" + signature);
    }

    private static void verifyContentIsHashed(HttpRequest httpRequest) {
        boolean hasContent = httpRequest instanceof ClassicHttpRequest && ((ClassicHttpRequest) httpRequest).getEntity() != null;
        if (hasContent && !httpRequest.containsHeader(Headers.X_Content_SHA256)) {
            throw new IllegalStateException(
                    "Refusing to sign a request with content, but without the " + Headers.X_Content_SHA256 + " header. " +
                    RequestContentHashInterceptor.class.getSimpleName() + " must be registered before " +
                    RequestSignatureInterceptor.class.getSimpleName() + ".");
        }
    }

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) throws IOException {
        verifyContentIsHashed(httpRequest);
        setSignatureHeader(httpRequest);
    }
}
