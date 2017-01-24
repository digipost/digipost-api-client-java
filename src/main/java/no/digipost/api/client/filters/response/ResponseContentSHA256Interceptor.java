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
import no.digipost.api.client.util.LoggingUtil;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static no.digipost.api.client.Headers.X_Content_SHA256;
import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseContentSHA256Interceptor implements HttpResponseInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseContentSHA256Interceptor.class);

    private boolean shouldThrow = true;

    public void setThrowOnError(final boolean shouldThrow) {
        this.shouldThrow = shouldThrow;
    }


    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (response.getEntity() != null) {
            try {
                validerContentHash(response);
            } catch (Exception e) {
                LoggingUtil.logResponse(response);
                logOrThrow("Det skjedde en feil under signatursjekk: " + e.getMessage(), e);
            }
        }
    }

    private void validerContentHash(final HttpResponse response) {
        try {
            String hashHeader = null;
            Header firstHeader = response.getFirstHeader(X_Content_SHA256);
            if(firstHeader != null){
                hashHeader = firstHeader.getValue();
            }

            if (isBlank(hashHeader)) {
                throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                        "Mangler X-Content-SHA256-header - server-signatur kunne ikke valideres");
            }
            byte[] entityBytes = Optional.ofNullable(EntityUtils.toByteArray(response.getEntity())).orElseGet(() -> new byte[0]);
            response.setEntity(new ByteArrayEntity(entityBytes));
            validerBytesMotHashHeader(hashHeader, entityBytes);
        } catch (IOException e) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                    "Det skjedde en feil under uthenting av innhold for validering av X-Content-SHA256-header - server-signatur kunne ikke valideres");
        }
    }

    private void validerBytesMotHashHeader(final String serverHash, final byte[] entityBytes) {
        SHA256Digest digest = new SHA256Digest();

        digest.update(entityBytes, 0, entityBytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        String ourHash = new String(Base64.encode(result));
        if (!serverHash.equals(ourHash)) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR,
                    "X-Content-SHA256-header matchet ikke innholdet - server-signatur er feil.");
        }
    }

    private void logOrThrow(final String message, final Exception e) {
        if (shouldThrow) {
            if (e instanceof DigipostClientException) {
                throw (DigipostClientException) e;
            } else {
                throw new DigipostClientException(SERVER_SIGNATURE_ERROR, message);
            }
        } else {
            LOG.warn("Feil under validering av server signatur: '" + e.getMessage() + "'. " +
                    (LOG.isDebugEnabled() ? "" : "Konfigurer debug-logging for " + LOG.getName() + " for å se full stacktrace."));
            LOG.debug(e.getMessage(), e);
        }
    }
}
