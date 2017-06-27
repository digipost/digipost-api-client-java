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
package no.digipost.api.client.representations.xml;

import no.digipost.api.datatypes.DataType;
import no.digipost.api.datatypes.types.Residence;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;

public class DataTypeXmlAdapterTest {

    private final DataTypeXmlAdapter adapter = new DataTypeXmlAdapter();

    @Test
    public void check_expected_marshalled_xmlstructure() throws Exception {
        DataType dt = Residence.EXAMPLE;
        String expectedXml = "<ns2:residence xmlns:ns2=\"http://api.digipost.no/schema/datatypes\"><address>" +
                "<streetAddress>Storgata 23</streetAddress><postalCode>0011</postalCode><city>Oslo</city></address>" +
                "<source>boligmappa</source><externalId>externalId</externalId></ns2:residence>";
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(adapter.marshal(dt)), new StreamResult(writer));
        String newXml = writer.toString();
        Assert.assertEquals(expectedXml, newXml);
    }
}
