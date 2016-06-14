package no.digipost.api.client.userdocuments;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;

public class MarshallingTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void shouldMarshallUnmarshallAgreement() {
		final StringWriter xml = new StringWriter();
		JAXB.marshal(Agreement.createInvoiceBankAgreement("01017012345", true), xml);
		log.debug(xml.toString());
		JAXB.unmarshal(new StringReader(xml.toString()), Agreement.class);
	}
}
