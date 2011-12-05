package no.digipost.api.client.representations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "value" })
@XmlRootElement(name = "encryptionKey")
public class EncryptionKey extends Representation {

	@XmlValue
	protected String value;
	@XmlAttribute(name = "keyId")
	protected String keyId;

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(final String value) {
		keyId = value;
	}

}
