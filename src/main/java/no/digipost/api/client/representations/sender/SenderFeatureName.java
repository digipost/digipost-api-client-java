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
public final class SenderFeatureName {

	/**
	 * Avsender kan sende post til mottakere i Digipost.
	 */
	public static final SenderFeatureName DIGIPOST_DELIVERY = new SenderFeatureName("no.digipost.feature.delivery.digipost", false);

	/**
	 * Avsender kan sende post til mottakere i Digipost, og har avtale om levering i fysisk post
	 * dersom mottaker ikke har Digipost-konto.
	 */
	public static final SenderFeatureName DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK = new SenderFeatureName("no.digipost.feature.delivery.digipost-with-print-fallback", false);

	/**
	 * Avsender kan velge å sende direkte til print og fysisk levering av post, uten
	 * å forsøke å levere i Digipost.
	 */
	public static final SenderFeatureName DELIVERY_DIRECT_TO_PRINT = new SenderFeatureName("no.digipost.feature.delivery.direct-to-print", false);

	/**
	 * For brev som skal til print og fysisk levering vil
	 * PDF-versjoner som ikke offisielt er støttet vil bli avvist.
	 */
	public static final SenderFeatureName PRINTVALIDATION_PDFVERSION = new SenderFeatureName("no.digipost.feature.validation.print.pdf.version", false);

	/**
	 * For brev som skal til print og fysisk levering vil
	 * fontoppsett i dokumentet bli validert.
	 */
	public static final SenderFeatureName PRINTVALIDATION_FONTS = new SenderFeatureName("no.digipost.feature.validation.print.fonts", false);

	/**
	 * For brev som skal til print og fysisk levering kan dokumentene
	 * ikke ha mer enn et fastsatt antall sider. Denne maksgrensen er per 07.08.2015 på 14 sider.
	 *
	 * Se
	 * <a href="https://github.com/digipost/printability-validator/blob/1.0.3/src/main/java/no/digipost/print/validate/PdfValidationSettings.java#L25">
	 * github.com/digipost/printability-validator/blob/1.0.3/src/main/java/no/digipost/print/validate/PdfValidationSettings.java#L25
	 * </a>
	 */
	public static final SenderFeatureName PRINTVALIDATION_PAGEAMOUNT = new SenderFeatureName("no.digipost.feature.validation.print.pageamount", false);

	/**
	 * For brev som skal til print og fysisk levering vil dokumenter med
	 * for smal venstremarg til å få plass til EA-strekkode bli avvist.
	 */
	public static final SenderFeatureName PRINTVALIDATION_MARGINS_LEFT = new SenderFeatureName("no.digipost.feature.validation.print.margins.left", false);

	private static final Elements<SenderFeatureName> KNOWN_FEATURES = on(
			DIGIPOST_DELIVERY, DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK, DELIVERY_DIRECT_TO_PRINT,
			PRINTVALIDATION_FONTS, PRINTVALIDATION_MARGINS_LEFT, PRINTVALIDATION_PAGEAMOUNT, PRINTVALIDATION_PDFVERSION);

	public final String identificator;
	private final boolean custom;

	private SenderFeatureName(String identificator, boolean custom) {
		this.identificator = identificator;
		this.custom = custom;
	}


	public static SenderFeatureName from(String identificator) {
		for(SenderFeatureName known : KNOWN_FEATURES.filter(where(getIdentificator, is(identificator))).head()) {
			return known;
		}
		return new SenderFeatureName(identificator, true);
	}

	public SenderFeature withParam(String param) {
		return new SenderFeature(this, param);
	}

	public SenderFeature withNoParam() {
		return withParam(null);
	}

	public static final Fn<String, SenderFeatureName> toSenderFeature = new Fn<String, SenderFeatureName>() {
		@Override public SenderFeatureName $(String identificator) { return from(identificator); }};


	public static final Fn<SenderFeatureName, String> getIdentificator = new Fn<SenderFeatureName, String>() {
		@Override public String $(SenderFeatureName feature) { return feature.identificator; }};

	@Override
	public String toString() {
		return identificator + (custom ? " (custom)" : "");
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SenderFeatureName &&
				Objects.equals(((SenderFeatureName) o).identificator, this.identificator) &&
				((SenderFeatureName) o).custom == this.custom;
	}

	@Override
	public int hashCode() {
		return Objects.hash(identificator, custom);
	}
}
