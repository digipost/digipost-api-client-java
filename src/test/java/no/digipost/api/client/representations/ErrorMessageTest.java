package no.digipost.api.client.representations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.ErrorType;

import org.junit.Test;

public class ErrorMessageTest {

	@Test
	public void shouldThrowExceptionIfCreatePrintMessageLinkNotAvailable() {
		try {
			new ErrorMessage("Error").getCreatePrintMessageLink();
		} catch (DigipostClientException e) {
			assertEquals(ErrorType.NOT_AUTHORIZED_FOR_PRINT, e.getErrorType());
			return;
		}
		fail("Should have thrown not authorized exception when the create print message link is not available.");
	}

}
