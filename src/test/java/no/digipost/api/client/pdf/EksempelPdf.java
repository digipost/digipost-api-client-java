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
package no.digipost.api.client.pdf;

import java.io.InputStream;

import static org.apache.commons.lang3.Validate.notNull;

public final class EksempelPdf {

    public static InputStream printablePdf1Page() {
        return notNull(EksempelPdf.class.getResourceAsStream("/pdf/a4-left-margin-20mm.pdf"), "not found");
    }

    public static InputStream printablePdf2Pages() {
        return notNull(EksempelPdf.class.getResourceAsStream("/pdf/a4-2pages.pdf"), "not found");
    }

    public static InputStream pdf20Pages() {
        return notNull(EksempelPdf.class.getResourceAsStream("/pdf/a4-20pages.pdf"), "not found");
    }

}
