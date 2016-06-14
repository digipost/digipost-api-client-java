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
package no.digipost.api.client.userdocuments;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

public class MarshallingTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Test
	public void shouldMarshallUnmarshallAgreement() {
		final StringWriter xml = new StringWriter();
		JAXB.marshal(Agreement.createInvoiceBankAgreement(new UserId("01017012345"), true), xml);
		log.debug(xml.toString());
		JAXB.unmarshal(new StringReader(xml.toString()), Agreement.class);
	}

	@Test
	public void shouldMarshallUnmarshallAgreements() {
		final StringWriter xml = new StringWriter();
		final Agreements agreements = new Agreements(Collections.singletonList(Agreement.createInvoiceBankAgreement(new UserId("01017012345"), true)));
		JAXB.marshal(agreements, xml);
		log.debug(xml.toString());
		JAXB.unmarshal(new StringReader(xml.toString()), Agreements.class);
	}
}
