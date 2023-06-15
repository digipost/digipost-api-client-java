/*
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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;


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
     * Behandler kan sende masseutsendelser.
     */
    public static final SenderFeatureName MASSEUTSENDELSE = new SenderFeatureName("no.digipost.feature.batch.masseutsendelse", false);

    /**
     * Behandler kan sende mottakersplitt.
     */
    public static final SenderFeatureName MOTTAKERSPLITT = new SenderFeatureName("no.digipost.feature.batch.mottakersplitt", false);

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
     * Kan avsender sende faktura uten KID? KID er normalt sett påkrevet i Digipost, men en virksomhet kan flagges for å ha lov til å sende uten KID.
     */
    public static final SenderFeatureName DIGIPOSTVALIDATION_INVOICE_ALLOW_NO_KID = new SenderFeatureName("no.digipost.feature.validation.digipost.invoice-allow-no-kid", false);

    /**
     * Max filstørrelse for sending til Digipost for virksomheter.
     */
    public static final SenderFeatureName DIGIPOSTVALIDATION_FILE_SIZE = new SenderFeatureName("no.digipost.feature.validation.digipost.file-size", false);

    /**
     * Filtyper som er lov å sende inn til Digipost for avsender.
     */
    public static final SenderFeatureName DIGIPOSTVALIDATION_ALLOWED_FILE_TYPES = new SenderFeatureName("no.digipost.feature.validation.digipost.allowed-file-types", false);

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

    /**
     * For brev som skal til print og fysisk levering vil dokumenter som
     * ikke er A4 bli avvist, bleed kan brukes til å mjuke upp kraven.
     */
    public static final SenderFeatureName PRINTVALIDATION_POSITIVE_BLEED = new SenderFeatureName("no.digipost.feature.validation.print.bleed", false);

    /**
     * For brev som skal til print og fysisk levering vil dokumenter som
     * ikke er A4 bli avvist, bleed kan brukes til å mjuka upp kraven.
     */
    public static final SenderFeatureName PRINTVALIDATION_NEGATIVE_BLEED = new SenderFeatureName("no.digipost.feature.validation.print.negativebleed", false);

    private static final Map<String, SenderFeatureName> KNOWN_FEATURES = Stream.of(
            DIGIPOST_DELIVERY, DIGIPOST_DELIVERY_WITH_PRINT_FALLBACK, DELIVERY_DIRECT_TO_PRINT, MASSEUTSENDELSE, MOTTAKERSPLITT,
            PRINTVALIDATION_FONTS, PRINTVALIDATION_MARGINS_LEFT, PRINTVALIDATION_PAGEAMOUNT, PRINTVALIDATION_PDFVERSION, PRINTVALIDATION_POSITIVE_BLEED,
            PRINTVALIDATION_NEGATIVE_BLEED, DIGIPOSTVALIDATION_INVOICE_ALLOW_NO_KID, DIGIPOSTVALIDATION_FILE_SIZE, DIGIPOSTVALIDATION_ALLOWED_FILE_TYPES)
                .collect(collectingAndThen(toMap((SenderFeatureName name) -> name.identificator, identity()), Collections::unmodifiableMap));

    public final String identificator;
    private final boolean custom;

    private SenderFeatureName(String identificator, boolean custom) {
        this.identificator = identificator;
        this.custom = custom;
    }


    public static SenderFeatureName from(String identificator) {
        SenderFeatureName known = KNOWN_FEATURES.get(identificator);
        return known != null ? known : new SenderFeatureName(identificator, true);
    }

    public SenderFeature withParam(String param) {
        return new SenderFeature(this, param);
    }

    public SenderFeature withNoParam() {
        return withParam(null);
    }

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
