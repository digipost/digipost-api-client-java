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
package no.digipost.api.client.representations;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public final class FileType {
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[a-z0-9\\-_]+$");

    public static final FileType PDF = new FileType("pdf");
    public static final FileType HTM = new FileType("htm");
    public static final FileType HTML = new FileType("html");
    public static final FileType XHTML = new FileType("xhtml");
    public static final FileType XHT = new FileType("xht");
    public static final FileType PNG = new FileType("png");
    public static final FileType JPG = new FileType("jpg");
    public static final FileType JPEG = new FileType("jpeg");
    public static final FileType GIF = new FileType("gif");
    public static final FileType ZIP = new FileType("zip");
    public static final FileType EHF = new FileType("ehf");

    private final String fileType;

    public FileType(final String fileType) {
        String normalized = trimToEmpty(fileType).toLowerCase();
        if (normalized.length() >= 30 || !ALLOWED_CHARACTERS.matcher(normalized).matches()) {
            this.fileType = "";
        } else {
            this.fileType = normalized;
        }
    }

    @Override
    public String toString() {
        return fileType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fileType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || !(obj instanceof FileType)) {
            return false;
        }
        FileType other = (FileType) obj;
        return Objects.equals(other.fileType, this.fileType);
    }

    public boolean isBlank() {
        return StringUtils.isBlank(fileType);
    }

    public static FileType fromFilename(String filename) {
        return new FileType(getExtension(filename));
    }
}
