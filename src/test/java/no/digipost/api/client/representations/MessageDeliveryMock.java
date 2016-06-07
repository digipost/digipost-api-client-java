package no.digipost.api.client.representations;

import java.util.List;

public class MessageDeliveryMock extends MessageDelivery {

	public static MessageDelivery setMessageDeliveryStatus(MessageDelivery messageDelivery, final Document primaryDocument, List<Document> attachments, final Link link){
		messageDelivery.primaryDocument = primaryDocument;
		messageDelivery.attachments = attachments;
		messageDelivery.addLink(link);

		return messageDelivery;
	}
}
