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
package no.posten.dpost.api.client.representations;

import javax.xml.bind.annotation.XmlEnum;

@XmlEnum
public enum AuthenticationLevel {
	PASSWORD(2),
	TWO_FACTOR(3);

	private final int level;

	AuthenticationLevel(final int level) {
		this.level = level;
	}

	public int level() {
		return level;
	}

	public static AuthenticationLevel fromLevel(final int verdi) {
		switch (verdi) {
		case 2:
			return PASSWORD;
		case 3:
			return TWO_FACTOR;
		default:
			throw new IllegalArgumentException("Unknown authentication level: " + verdi);
		}
	}

	public String value() {
		return name();
	}

	public static AuthenticationLevel fromValue(final String v) {
		return valueOf(v);
	}
}
