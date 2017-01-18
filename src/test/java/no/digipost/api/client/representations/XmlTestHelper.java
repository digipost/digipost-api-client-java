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
package no.digipost.api.client.representations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class XmlTestHelper {

	private static final Logger LOG = LoggerFactory.getLogger(XmlTestHelper.class);


	public static <T> T marshallValidateAndUnmarshall(T element) {
		return marshallValidateAndUnmarshall(element, false);
	}

	public static <T> T marshallValidateAndUnmarshall(T element, boolean log) {
		try (ByteArrayOutputStream resultXml = new ByteArrayOutputStream()) {
			Marshaller marshaller = Singletons.jaxbContext.createMarshaller();
			marshaller.setSchema(Singletons.schema);
			marshaller.setProperty("jaxb.formatted.output", true);
			marshaller.marshal(element, new StreamResult(resultXml));
			resultXml.flush();
			byte[] xml = resultXml.toByteArray();
			if (log) {
				LOG.info("Marshalled XML:\n{}", new String(xml));
			}
			try (InputStream in = new ByteArrayInputStream(xml)) {
				Unmarshaller unmarshaller = Singletons.jaxbContext.createUnmarshaller();
				unmarshaller.setSchema(Singletons.schema);
				@SuppressWarnings("unchecked")
				T unmarshalled = (T) unmarshaller.unmarshal(in);
				return unmarshalled;
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	/**
	 * Inner class to achieve lazy initialization of static singletons.
	 */
	private static final class Singletons {

		static final Schema schema;
		static final JAXBContext jaxbContext;

		static {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
	            schema = schemaFactory.newSchema(XmlTestHelper.class.getResource("/xsd/api_v7.xsd"));
	            jaxbContext = JAXBContext.newInstance("no.digipost.api.client.representations:no.digipost.api.client.representations.sender");
            } catch (SAXException | JAXBException e) {
	            throw new RuntimeException(e.getMessage(), e);
            }
		}
	}


}
