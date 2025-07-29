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
package no.digipost.api.client.internal.delivery;

import no.digipost.api.client.archive.ArchiveApi;
import no.digipost.api.client.internal.http.StatusLineMock;
import no.digipost.api.client.pdf.EksempelPdf;
import no.digipost.api.client.representations.archive.Archive;
import no.digipost.api.client.representations.archive.ArchiveDocument;
import no.digipost.time.ControllableClock;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

import static no.digipost.api.client.DigipostClientConfig.newConfiguration;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;
import static no.digipost.api.client.util.JAXBContextUtils.marshal;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchiveDelivererTest {

    private final ControllableClock clock = ControllableClock.freezedAt(Instant.now());

    @Mock
    ArchiveApi archiveApi;

    @Test
    void should_send_and_marchall_response() {

        final ArchiveDocument vedlegg = new ArchiveDocument(
                UUID.randomUUID()
                , "vedlegg_123123.pdf"
                , "pdf"
                , "application/pdf"
        ).withReferenceId("234234235234235")
                .withDeletionTime(ZonedDateTime.now(clock).plusMonths(6));

        final Archive archive = Archive.defaultArchive()
                .documents(vedlegg)
                .build();

        CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
        ByteArrayOutputStream bao2 = new ByteArrayOutputStream();
        marshal(jaxbContext, archive, bao2);
        when(response.getEntity()).thenReturn(new ByteArrayEntity(bao2.toByteArray()));
        when(response.getStatusLine()).thenReturn(new StatusLineMock(200));
        when(archiveApi.sendMultipartArchive(any(HttpEntity.class))).thenReturn(response);

        final Archive archiveResponse = new ArchiveDeliverer(newConfiguration().clock(clock).build(), archiveApi)
                .createArchive(archive).addFile(vedlegg, EksempelPdf.pdf20Pages())
                .send();

        assertThat(archive, not(sameInstance(archiveResponse)));
        verify(archiveApi).sendMultipartArchive(any(HttpEntity.class));
    }
}
