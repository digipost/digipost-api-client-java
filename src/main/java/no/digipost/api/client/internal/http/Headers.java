/*
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
package no.digipost.api.client.internal.http;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import static no.digipost.api.client.representations.MediaTypes.DIGIPOST_MEDIA_TYPE_V8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

public final class Headers {

    private static final String X_Digipost_Prefix = "X-Digipost-";

    public static final String X_Digipost_Signature = X_Digipost_Prefix + "Signature";
    public static final String X_Digipost_UserId = X_Digipost_Prefix + "UserId";

    public static final String Content_MD5 = "Content-MD5";
    public static final String X_Content_SHA256 = "X-Content-SHA256";

    public static final Header Accept_DIGIPOST_MEDIA_TYPE_V8 = new BasicHeader(ACCEPT, DIGIPOST_MEDIA_TYPE_V8);
    public static final Header Content_Type_DIGIPOST_MEDIA_TYPE_V8 = new BasicHeader(CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_V8);

}
