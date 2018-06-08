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
package no.digipost.api.client.util;

import no.digipost.api.client.representations.*;
import no.digipost.api.client.representations.accounts.UserInformation;
import no.digipost.api.client.representations.accounts.UserAccount;
import no.digipost.api.client.representations.sender.SenderInformation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.io.InputStream;
import java.io.OutputStream;

import static no.digipost.api.client.util.ExceptionUtils.exceptionNameAndMessage;

public class JAXBContextUtils {
    public static final JAXBContext jaxbContext = initContext(
            EntryPoint.class, ErrorMessage.class, Identification.class, IdentificationResult.class, Message.class, Recipients.class,
            Autocomplete.class, DocumentEvents.class, DocumentStatus.class, MessageDelivery.class, EncryptionKey.class,
            IdentificationResultWithEncryptionKey.class, SenderInformation.class, UserInformation.class, UserAccount.class
    );

    private static JAXBContext initContext(Class<?>... clazz) {
        try {
            return JAXBContext.newInstance(clazz);
        } catch (JAXBException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void marshal(JAXBContext context, Object objectToMarshall, OutputStream outputStream){
        try {
            context.createMarshaller().marshal(objectToMarshall, outputStream);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed when trying to marshal object to outputstream. Cause: " + exceptionNameAndMessage(e), e);
        }
    }

    public static <T> T unmarshal(JAXBContext context, InputStream inputStream, Class<T> type){
        try {
            return type.cast(context.createUnmarshaller().unmarshal(inputStream));
        } catch (JAXBException e) {
            throw new RuntimeException("Failed when trying to unmarshal inputstream to object. Cause: " + exceptionNameAndMessage(e), e);
        }
    }
}
