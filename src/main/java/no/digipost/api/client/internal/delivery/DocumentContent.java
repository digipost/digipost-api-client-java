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
package no.digipost.api.client.internal.delivery;

import java.io.InputStream;

final class DocumentContent {
    private final InputStream digipostContent;
    private final InputStream printContent;

    private DocumentContent(InputStream digipostContent, InputStream printContent){
        this.digipostContent = digipostContent;
        this.printContent = printContent;
    }

    public InputStream getPrintContent(){
        if(printContent == null){
            throw new IllegalAccessError("Content ikke tilgjengelig, dette er et digipost DocumentContent");
        }

        return printContent;
    }

    public InputStream getDigipostContent(){
        if(digipostContent == null){
            throw new IllegalAccessError("Content ikke tilgjengelig, dette er et print DocumentContent");
        }

        return digipostContent;
    }

    public static DocumentContent CreateDigiPostContent(InputStream content){
        return new DocumentContent(content, null);
    }

    public static DocumentContent CreatePrintContent(InputStream content){
        return new DocumentContent(null, content);
    }

    public static DocumentContent CreateMultiStreamContent(InputStream digipostContent, InputStream printContent){
        return new DocumentContent(digipostContent, printContent);
    }

    public static DocumentContent CreateBothStreamContent(InputStream content){
        return new DocumentContent(content, content);
    }
}
