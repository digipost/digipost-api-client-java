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
package no.digipost.api.client.internal.http.response.interceptor;

import no.digipost.api.client.errorhandling.DigipostClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.util.Optional;

import static no.digipost.api.client.errorhandling.ErrorCode.SERVER_SIGNATURE_ERROR;
import static no.digipost.api.client.internal.http.Headers.X_Content_SHA256;

public class ResponseContentSHA256Interceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
        ClassicHttpResponse classicHttpResponse = (ClassicHttpResponse) response;
        final HttpEntity entity = classicHttpResponse.getEntity();
        if (entity != null && entity.getContent() != null && entity.getContentLength() > 0) {
            String hashHeaderValue = Optional.ofNullable(response.getFirstHeader(X_Content_SHA256))
                    .map(NameValuePair::getValue)
                    .filter(StringUtils::isNoneBlank)
                    .orElseThrow(() -> new DigipostClientException(SERVER_SIGNATURE_ERROR,
                            String.format("Missing %s header in response. This header is expected when a response body is present. Http status was %s",
                                    X_Content_SHA256, response.getCode())));
            byte[] entityBytes = EntityUtils.toByteArray(entity);
            validerBytesMotHashHeader(hashHeaderValue, entityBytes);
            classicHttpResponse.setEntity(new ByteArrayEntity(entityBytes, ContentType.parse(entityDetails.getContentType())));
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
