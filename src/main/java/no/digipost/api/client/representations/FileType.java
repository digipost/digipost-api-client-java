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

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FileType {
	private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^[a-zA-Z0-9\\-_]+$");

	public static final FileType PDF = new FileType("pdf");
	public static final FileType HTM = new FileType("htm");
	public static final FileType HTML = new FileType("html");
	public static final FileType XHTML = new FileType("xhtml");
	public static final FileType XHT = new FileType("xht");
	public static final FileType PNG = new FileType("png");
	public static final FileType JPG = new FileType("jpg");
	public static final FileType JPEG = new FileType("jpeg");
	public static final FileType GIF = new FileType("gif");

	private final String fileType;

	public FileType(final String fileType) {
		if (fileType == null || fileType.length() >= 30 || !ALLOWED_CHARACTERS.matcher(fileType).matches()) {
			this.fileType = "";
		} else {
			this.fileType = fileType.toLowerCase();
		}
	}

	@Override
	public String toString() {
		return isBlank() ? "" : fileType;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(fileType).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FileType other = (FileType) obj;
		return new EqualsBuilder().append(fileType, other.fileType).isEquals();
	}

	public boolean isBlank() {
		return StringUtils.isBlank(fileType);
	}

	public static FileType fromFilename(final String filename) {
		return new FileType(getExtension(filename));
	}
}
