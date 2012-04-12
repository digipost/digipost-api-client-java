package no.digipost.api.client;

import no.digipost.api.client.representations.ErrorMessage;

public class DigipostClientServerException extends DigipostClientException {

	private static final long serialVersionUID = 1L;

	private final ErrorMessage errorMessage;

	public DigipostClientServerException(final ErrorType errorType, final ErrorMessage errorMessage) {
		super(errorType, errorMessage.getErrorMessage());
		this.errorMessage = errorMessage;
	}

	public ErrorMessage getErrorMessageEntity() {
		return errorMessage;
	}

}
