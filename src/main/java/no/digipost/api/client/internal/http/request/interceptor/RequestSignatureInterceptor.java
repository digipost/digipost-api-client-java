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
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class RequestSignatureInterceptor implements HttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestSignatureInterceptor.class);

    private final Signer signer;
    private final RequestContentHashFilter hashFilter;
    private final EventLogger eventLogger;

    public RequestSignatureInterceptor(Signer signer, RequestContentHashFilter hashFilter) {
        this(signer, EventLogger.NOOP_LOGGER, hashFilter);
    }

    public RequestSignatureInterceptor(Signer signer, EventLogger eventLogger, RequestContentHashFilter hashFilter){
        this.eventLogger = (eventLogger != null ? eventLogger : EventLogger.NOOP_LOGGER).withDebugLogTo(LOG);
        this.signer = signer;
        this.hashFilter = hashFilter;
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

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws IOException {

        if(httpRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) httpRequest;
            HttpEntity rqEntity = request.getEntity();

            if (rqEntity == null) {
                setSignatureHeader(httpRequest);
            } else {
                byte[] entityBytes = Optional.ofNullable(EntityUtils.toByteArray(rqEntity)).orElseGet(() -> new byte[0]);
                hashFilter.settContentHashHeader(entityBytes, request);
                setSignatureHeader(httpRequest);
            }
        } else {
            setSignatureHeader(httpRequest);
        }
        httpContext.setAttribute("request-path", URI.create(httpRequest.getRequestLine().getUri()).getPath());


    }
}
