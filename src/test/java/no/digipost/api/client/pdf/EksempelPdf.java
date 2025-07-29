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
package no.digipost.api.client.pdf;

import java.io.InputStream;

import static no.digipost.DiggBase.nonNull;

public final class EksempelPdf {

    public static InputStream printablePdf1Page() {
        return nonNull("/pdf/a4-left-margin-20mm.pdf", EksempelPdf.class::getResourceAsStream);
    }

    public static InputStream printablePdf2Pages() {
        return nonNull("/pdf/a4-2pages.pdf", EksempelPdf.class::getResourceAsStream);
    }

    public static InputStream pdf20Pages() {
        return nonNull("/pdf/a4-20pages.pdf", EksempelPdf.class::getResourceAsStream);
    }

    public static InputStream pdf30Pages() {
        return nonNull("/pdf/a4-30pages.pdf", EksempelPdf.class::getResourceAsStream);
    }

}
