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
package no.digipost.api.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.ws.rs.core.Response.Status;

import no.digipost.api.client.DigipostClientException.ErrorType;
import no.digipost.api.client.representations.EncryptionKey;
import no.digipost.api.client.representations.ErrorMessage;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.MessageBase;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class Communicator {

	private static final Logger LOG = LoggerFactory.getLogger(Communicator.class);

	protected final EventLogger eventLogger;
	protected final ApiService apiService;

	public Communicator(final ApiService apiService, final EventLogger eventLogger) {
		this.apiService = apiService;
		this.eventLogger = eventLogger;
	}

	private static OutputEncryptor buildEncryptor() throws CMSException {
		return new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
	}

	/**
	 * Krypterer <code>data</code> med brukers pubklike nøkkel
	 * 
	 * @param data
	 * @param keyId
	 * @param keyContent
	 * @return
	 * @throws Exception
	 */
	private byte[] preencrypt(final byte[] data, final String keyId, final String keyContent) throws Exception {
		PEMReader reader = new PEMReader(new StringReader(keyContent));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(((JCERSAPublicKey) reader.readObject()).getEncoded());
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

		CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
		gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(keyId.getBytes(), publicKey));
		CMSEnvelopedData d = gen.generate(new CMSProcessableByteArray(data), buildEncryptor());
		return d.getEncoded();
	}

	protected void checkResponse(final ClientResponse response) {
		Status status = Status.fromStatusCode(response.getStatus());
		if (!responseOk(response)) {
			String errorMessage = fetchErrorMessageString(response);
			log(errorMessage);
			switch (status) {
			case BAD_REQUEST:
				throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST, errorMessage);
			case CONFLICT:
				throw new DigipostClientException(ErrorType.INVALID_TRANSACTION, errorMessage);
			case INTERNAL_SERVER_ERROR:
				throw new DigipostClientException(ErrorType.SERVER_ERROR, errorMessage);
			default:
				throw new DigipostClientException(ErrorType.GENERAL_ERROR, errorMessage);
			}
		}
	}

	protected String fetchErrorMessageString(final ClientResponse response) {
		try {
			return response.getEntity(ErrorMessage.class).getErrorMessage();
		} catch (ClientHandlerException e) {
			throw new DigipostClientException(ErrorType.SERVER_ERROR,
					"Det skjedde en feil på serveren, men klienten kunne ikke lese responsen.");
		} catch (UniformInterfaceException e) {
			return "";
		}
	}

	private boolean responseOk(final ClientResponse response) {
		Status status = Status.fromStatusCode(response.getStatus());
		if (status == null) {
			return false;
		}
		switch (status) {
		case CREATED:
		case OK:
			return true;
		default:
			return false;
		}
	}

	protected void log(final String message) {
		LOG.debug(message);
		eventLogger.log(message);
	}

	protected void logThrowable(final Throwable t) {
		LOG.debug("Feil.", t);

		StringWriter stacktrace = new StringWriter();
		t.printStackTrace(new PrintWriter(stacktrace));
		eventLogger.log(stacktrace.toString());
	}

	protected boolean messageAlreadyExists(final ClientResponse response) {
		return Status.CONFLICT.equals(Status.fromStatusCode(response.getStatus()));
	}

	protected void checkThatExistingMessageIsIdenticalToNewMessage(final MessageBase exisitingMessage, final MessageBase message) {
		if (!exisitingMessage.isIdenticalTo(message)) {
			String errorMessage = "Forsendelse med id [" + message.getMessageId() + "] finnes fra før med annen spesifikasjon.";
			log(errorMessage);
			throw new DigipostClientException(ErrorType.DUPLICATE_MESSAGE_ID, errorMessage);
		}
	}

	protected void checkThatMessageCanBePreEncrypted(final MessageBase message) {
		Link encryptionKeyLink = message.getEncryptionKeyLink();
		if (encryptionKeyLink == null) {
			String errorMessage = "Forsendelse med id [" + message.getMessageId() + "] kan ikke prekrypteres.";
			log(errorMessage);
			throw new DigipostClientException(ErrorType.CANNOT_PREENCRYPT, errorMessage);
		}
	}

	/**
	 * Henter brukers publike nøkkel fra servern og krypterer brevet som skal
	 * sendes med denne.
	 */
	public InputStream fetchKeyAndEncrypt(final MessageBase message, final InputStream content) {
		checkThatMessageCanBePreEncrypted(message);

		ClientResponse encryptionKeyResponse = apiService.getEncryptionKey(message.getEncryptionKeyLink().getUri());
		checkResponse(encryptionKeyResponse);

		EncryptionKey key = encryptionKeyResponse.getEntity(EncryptionKey.class);

		try {
			byte[] encryptedContent = preencrypt(IOUtils.toByteArray(content), key.getKeyId(), key.getValue());
			return new ByteArrayInputStream(encryptedContent);
		} catch (Exception e) {
			logThrowable(e);
			throw new DigipostClientException(ErrorType.FAILED_PREENCRYPTION, "Inneholdet kunne ikke prekrypteres.");
		}
	}

	protected void check404Error(final ClientResponse response, final ErrorType errorBy404) {
		if (Status.fromStatusCode(response.getStatus()) == Status.NOT_FOUND) {
			throw new DigipostClientException(errorBy404, fetchErrorMessageString(response));
		}
	}

}