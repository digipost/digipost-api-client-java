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
package no.digipost.api.client.delivery;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.MessageDelivery;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * Operasjoner for å fullføre leveranse av en forsendelse. En forsendelse
 * kan enten være {@link WithPrintFallback til Digipost-mottakere} eller
 * sendes {@link ForPrintOnly direkte til print}.
 *
 * @param <OPERATIONS> Type som har et gyldig sett med operasjoner for denne
 *                     forsendelsen. Enten {@link SendableWithPrintFallback}
 *                     eller {@link SendableForPrintOnly}.
 *
 * @see WithPrintFallback
 * @see ForPrintOnly
 * @see DigipostClient#createMessage(no.digipost.api.client.representations.Message)
 * @see DigipostClient#createPrintOnlyMessage(no.digipost.api.client.representations.Message)
 */
public interface OngoingDelivery<OPERATIONS extends OngoingDelivery<OPERATIONS> & OngoingDelivery.SendableDelivery> {


    /**
     * Laster opp innhold til et dokument.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    OPERATIONS addContent(Document document, InputStream content);

    /**
     * Laster opp innhold til et dokument.
     *
     * @return videre operasjoner for å fullføre leveransen.
     */
    default OPERATIONS addContent(Document document, byte[] content) {
        return addContent(document, new ByteArrayInputStream(content));
    }


    /**
     * Operasjoner for å sende forsendelser direkte til print.
     * Alt dokumentinnhold i forsendelsen må være PDF.
     */
    public static interface ForPrintOnly extends OngoingDelivery<SendableForPrintOnly> {
    }



    /**
     * Operasjoner for å sende forsendelser til Digipost, men mulig fallback til print
     * dersom avsender har egnet avtale for dette.
     */
    public static interface WithPrintFallback extends OngoingDelivery<SendableWithPrintFallback> {

        /**
         * Laster opp innhold til et dokument med alternativt innhold for print, hvor sistnevnte må være PDF.
         *
         * @return videre operasjoner for å fullføre leveransen.
         */
        SendableWithPrintFallback addContent(Document document, InputStream content, InputStream printContent);

        /**
         * Laster opp innhold til et dokument med alternativt innhold for print, hvor sistnevnte må være PDF.
         *
         * @return videre operasjoner for å fullføre leveransen.
         */
        default SendableWithPrintFallback addContent(Document document, byte[] content, byte[] printContent) {
            return addContent(document, new ByteArrayInputStream(content), new ByteArrayInputStream(printContent));
        }
    }



    public static interface SendableDelivery {
        /**
         * Sender forsendelsen.
         */
        MessageDelivery send();
    }

    public static interface SendableForPrintOnly extends SendableDelivery, ForPrintOnly { }

    public static interface SendableWithPrintFallback extends SendableDelivery, WithPrintFallback { }

}
