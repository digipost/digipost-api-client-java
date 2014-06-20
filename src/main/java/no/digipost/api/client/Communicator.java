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

import no.digipost.api.client.errorhandling.ErrorType;

import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.DigipostClientServerException;
import no.digipost.api.client.representations.*;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Superklasse for MessageSender som har funksjonalitet for å snakke med
 * ApiService.
 *
 */
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

	private byte[] preencrypt(final byte[] data, final String keyId, final String keyContent) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(keyContent));
		SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
		X509EncodedKeySpec spec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
		IOUtils.closeQuietly(pemParser);
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

		CMSEnvelopedDataGenerator gen = new CMSEnvelopedDataGenerator();
		gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(keyId.getBytes(), publicKey));
		CMSEnvelopedData d = gen.generate(new CMSProcessableByteArray(data), buildEncryptor());
		return d.getEncoded();
	}

	protected void checkResponse(final Response response) {
		Status status = Status.fromStatusCode(response.getStatus());
		if (!responseOk(response)) {
			ErrorMessage error = fetchErrorMessageString(response);
			log(error.toString());
			switch (status) {
			case BAD_REQUEST:
				throw new DigipostClientException(ErrorType.PROBLEM_WITH_REQUEST, error.getErrorMessage());
			case CONFLICT:
				throw new DigipostClientException(ErrorType.INVALID_TRANSACTION, error.getErrorMessage());
			case INTERNAL_SERVER_ERROR:
				throw new DigipostClientException(ErrorType.SERVER_ERROR, error.getErrorMessage());
			default:
				throw new DigipostClientException(error);
			}
		}
	}

	protected ErrorMessage fetchErrorMessageString(final Response response) {
		try {
			return response.readEntity(ErrorMessage.class);
		} catch (ProcessingException | IllegalStateException | WebApplicationException e) {
			return new ErrorMessage("Det skjedde en feil på serveren, men klienten kunne ikke lese responsen.");
		}
	}

	private boolean responseOk(final Response response) {
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

	protected boolean resourceAlreadyExists(final Response response) {
		return Status.CONFLICT.equals(Status.fromStatusCode(response.getStatus()));
	}

	protected void checkThatExistingMessageIsIdenticalToNewMessage(final MessageDelivery exisitingMessage, final Message message) {
		if (!exisitingMessage.isSameMessageAs(message)) {
			String errorMessage = "Forsendelse med id [" + message.getMessageId() + "] finnes fra før med annen spesifikasjon.";
			log(errorMessage);
			throw new DigipostClientException(ErrorType.DUPLICATE_MESSAGE_ID, errorMessage);
		}
	}

	protected void checkThatMessageCanBePreEncrypted(final Document document) {
		Link encryptionKeyLink = document.getEncryptionKeyLink();
		if (encryptionKeyLink == null) {
			String errorMessage = "Document med id [" + document.getUuid() + "] kan ikke prekrypteres.";
			log(errorMessage);
			throw new DigipostClientException(ErrorType.CANNOT_PREENCRYPT, errorMessage);
		}
	}

	/**
	 * Henter brukers public nøkkel fra serveren og krypterer brevet som skal
	 * sendes med denne.
	 */
	public InputStream fetchKeyAndEncrypt(final Document document, final InputStream content) {
		checkThatMessageCanBePreEncrypted(document);

		Response encryptionKeyResponse = apiService.getEncryptionKey(document.getEncryptionKeyLink().getUri());

		checkResponse(encryptionKeyResponse);

		EncryptionKey key = encryptionKeyResponse.readEntity(EncryptionKey.class);

		try {
			byte[] encryptedContent = preencrypt(IOUtils.toByteArray(content), key.getKeyId(), key.getValue());
			return new ByteArrayInputStream(encryptedContent);
		} catch (Exception e) {
			logThrowable(e);
			throw new DigipostClientException(ErrorType.FAILED_PREENCRYPTION, "Inneholdet kunne ikke prekrypteres.");
		}
	}

	protected void check404Error(final Response response, final ErrorType errorBy404) {
		if (Status.fromStatusCode(response.getStatus()) == Status.NOT_FOUND) {
			throw new DigipostClientServerException(errorBy404, fetchErrorMessageEntity(response));
		}
	}

	private ErrorMessage fetchErrorMessageEntity(final Response response) {
		return response.readEntity(ErrorMessage.class);
	}

}