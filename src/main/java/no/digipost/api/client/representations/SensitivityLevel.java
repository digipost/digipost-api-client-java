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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "sensitivity-level")
@XmlEnum
public enum SensitivityLevel {
	NORMAL(0),
	SENSITIVE(1);

	private final int level;

	SensitivityLevel(final int level) {
		this.level = level;
	}

	public int level() {
		return level;
	}

	public static SensitivityLevel fromLevel(final int verdi) {
		switch (verdi) {
		case 0:
			return NORMAL;
		case 1:
			return SENSITIVE;
		default:
			throw new IllegalArgumentException("Unknown sensitivity level: " + verdi);
		}
	}

	public String value() {
		return name();
	}

	public static SensitivityLevel fromValue(final String v) {
		return valueOf(v);
	}
}
