package no.digipost.api.client.representations;

public class OrganisationNumber extends RecipientIdentifier {

	public OrganisationNumber(String identifier) {
		super(identifier);
	}

	@Override
	public boolean isPersonalIdentificationNumber() {
		return false;
	}

}
