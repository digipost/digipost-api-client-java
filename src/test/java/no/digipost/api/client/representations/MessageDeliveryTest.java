package no.digipost.api.client.representations;

import static java.util.Arrays.asList;
import static no.digipost.api.client.representations.DeliveryMethod.DIGIPOST;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.MessageStatus.NOT_COMPLETE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;

public class MessageDeliveryTest {

	@Test
    public void deliveryWithNoDocumentsYieldsEmptyListWithDocuments() {
	    MessageDelivery delivery = new MessageDelivery(null, DIGIPOST, NOT_COMPLETE, null);
	    assertThat(delivery.getAllDocuments(), empty());
	    assertThat(delivery.getAttachments(), empty());
    }

	@Test
    public void gettingAllDocumentsYieldsListWithPrimaryDocumentFirstFollowedByAttachments() {
		Document primary = new Document(UUID.randomUUID().toString(), "primary", PDF);
		Document att1 = new Document(UUID.randomUUID().toString(), "att1", PDF);
		Document att2 = new Document(UUID.randomUUID().toString(), "att2", PDF);

		MessageDelivery delivery = new MessageDelivery(null, DIGIPOST, NOT_COMPLETE, null);
		delivery.primaryDocument = primary;
		delivery.attachments = asList(att1, att2);

		assertThat(delivery.getAllDocuments(), contains(primary, att1, att2));
		assertThat(delivery.getAttachments(), contains(att1, att2));
    }

	@Test
    public void findingDocumentsByUuid() {
		Document primary = new Document(UUID.randomUUID().toString(), "primary", PDF);
		Document att1 = new Document(UUID.randomUUID().toString(), "att1", PDF);

		MessageDelivery delivery = new MessageDelivery(null, DIGIPOST, NOT_COMPLETE, null);
		delivery.primaryDocument = primary;
		delivery.attachments = asList(att1);

		assertThat(delivery.getDocumentByUuid(primary.getUuid()), is(primary));
		assertThat(delivery.getDocumentByUuid(att1.getUuid()), is(att1));

		try {
			delivery.getDocumentByUuid(UUID.randomUUID().toString());
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), containsString("not found"));
			return;
		}
		fail("should throw exception");
    }
}
