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
package no.digipost.api.client.representations;

public class MediaTypes {

    public static final String DIGIPOST_MEDIA_TYPE_V8 = "application/vnd.digipost-v8+xml";
    public static final String DIGIPOST_MULTI_MEDIA_SUB_TYPE_V8 = "vnd.digipost-v8+xml";
    public static final String DIGIPOST_MULTI_MEDIA_TYPE_V8 = "multipart/" + DIGIPOST_MULTI_MEDIA_SUB_TYPE_V8;

    public static final String APPLICATION_PDF = "application/pdf";

}
