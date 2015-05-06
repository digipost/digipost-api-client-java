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
package no.digipost.api.client.representations.sender;

import no.motif.f.Fn;
import no.motif.types.Elements;

import java.util.Objects;

import static no.motif.Base.is;
import static no.motif.Base.where;
import static no.motif.Iterate.on;


/**
 * Funksjonalitet som er støttet for en avsender.
 *
 * @see #DIGIPOST_DELIVERY
 * @see #DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK
 * @see #DELIVERY_DIRECT_TO_PRINT
 */
public final class SenderFeature {

	/**
	 * Avsender kan sende post til mottakere i Digipost.
	 */
	public static final SenderFeature DIGIPOST_DELIVERY = new SenderFeature("no.digipost.feature.delivery.digipost");

	/**
	 * Avsender kan sende post til mottakere i Digipost, og har avtale om levering i fysisk post
	 * dersom mottaker ikke har Digipost-konto.
	 */
	public static final SenderFeature DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK = new SenderFeature("no.digipost.feature.delivery.digipost-with-print-fallback");

	/**
	 * Avsender kan velge å sende direkte til print og fysisk levering av post, uten
	 * å forsøke å levere i Digipost.
	 */
	public static final SenderFeature DELIVERY_DIRECT_TO_PRINT = new SenderFeature("no.digipost.feature.delivery.direct-to-print");

	private static final Elements<SenderFeature> KNOWN_FEATURES = on(DIGIPOST_DELIVERY, DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK, DELIVERY_DIRECT_TO_PRINT);

	public final String identificator;

	private SenderFeature(String identificator) {
		this.identificator = identificator;
	}


	public static SenderFeature from(String identificator) {
		for(SenderFeature known : KNOWN_FEATURES.filter(where(getIdentificator, is(identificator))).head()) {
			return known;
		}
		return new SenderFeature(identificator);
	}

	public static final Fn<String, SenderFeature> toSenderFeature = new Fn<String, SenderFeature>() {
		@Override public SenderFeature $(String identificator) { return from(identificator); }};


	public static final Fn<SenderFeature, String> getIdentificator = new Fn<SenderFeature, String>() {
		@Override public String $(SenderFeature feature) { return feature.identificator; }};

	@Override
	public String toString() {
		return identificator;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SenderFeature && Objects.equals(((SenderFeature) o).identificator, this.identificator);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(identificator);
	}
}
