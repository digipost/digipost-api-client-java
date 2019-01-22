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
package no.digipost.api.client.internal.http.response.interceptor;

import no.digipost.api.client.errorhandling.DigipostClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.util.Optional;

import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static no.digipost.api.client.internal.http.Headers.X_Content_SHA256;

public class ResponseContentSHA256Interceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();
        if (entity != null && entity.getContent() != null && entity.getContentLength() > 0) {
            String hashHeaderValue = Optional.ofNullable(response.getFirstHeader(X_Content_SHA256))
                    .map(h -> h.getValue())
                    .filter(StringUtils::isNoneBlank)
                    .orElseThrow(() -> new DigipostClientException(SERVER_SIGNATURE_ERROR,
                            String.format("Missing %s header in response. This header is expected when a response body is present. Http response was %s",
                                    X_Content_SHA256, response.getStatusLine())));
            byte[] entityBytes = EntityUtils.toByteArray(entity);
            validerBytesMotHashHeader(hashHeaderValue, entityBytes);
            response.setEntity(new ByteArrayEntity(entityBytes));
        }
    }

    private void validerBytesMotHashHeader(final String serverHash, final byte[] entityBytes) {
        SHA256Digest digest = new SHA256Digest();

        digest.update(entityBytes, 0, entityBytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        String ourHash = new String(Base64.encode(result));
        if (!serverHash.equals(ourHash)) {
            throw new DigipostClientException(SERVER_SIGNATURE_ERROR, X_Content_SHA256 + " header did not match actual response body");
        }
    }
}
