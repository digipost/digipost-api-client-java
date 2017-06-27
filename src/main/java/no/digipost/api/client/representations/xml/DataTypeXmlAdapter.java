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
import no.digipost.api.datatypes.marshalling.DataTypesJAXBContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DataTypeXmlAdapter extends XmlAdapter<Element, DataType> {
    private JAXBContext jaxbContext;
    private DocumentBuilder documentBuilder;

    public DataTypeXmlAdapter() {
    }

    public DataTypeXmlAdapter(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    private JAXBContext getJAXBContext(Class<?> type) throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(type);
        }
        return jaxbContext;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if(documentBuilder == null) {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        return documentBuilder;
    }

    @Override
    public DataType unmarshal(Element v) throws Exception {
        return null;
    }

    @Override
    public Element marshal(DataType v) throws Exception {
        final QName qname = new QName(DataTypesJAXBContext.DIGIPOST_DATATYPES_NAMESPACE, v.getType().toLowerCase());
        Class<?> type = v.getClass();
        JAXBElement jaxbElement = new JAXBElement(qname, type, v);

        Document document = getDocumentBuilder().newDocument();
        Marshaller marshaller = getJAXBContext(type).createMarshaller();
        marshaller.marshal(jaxbElement, document);
        return document.getDocumentElement();
    }
}
