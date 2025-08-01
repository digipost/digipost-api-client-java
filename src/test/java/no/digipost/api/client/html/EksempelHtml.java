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
package no.digipost.api.client.html;

import java.io.InputStream;

import static org.apache.commons.lang3.Validate.notNull;

public final class EksempelHtml {

    public static InputStream validHtml() {
        return notNull(EksempelHtml.class.getResourceAsStream("/html/valid.html"), "not found");
    }
    
    public static InputStream validNotSanitizedHtml() {
        return notNull(EksempelHtml.class.getResourceAsStream("/html/validNotSanitized.html"), "not found");
    }

    public static InputStream illegalTags() {
        return notNull(EksempelHtml.class.getResourceAsStream("/html/illegalTags.html"), "not found");
    }

}
