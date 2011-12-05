package no.digipost.api.client.representations.xml;

import java.util.Date;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;

@XmlTransient
public class DateTimeXmlAdapter extends XmlAdapter<Date, DateTime> {

	@Override
	public Date marshal(final DateTime v) throws Exception {
		return v == null ? null : new Date(v.getMillis());
	}

	@Override
	public DateTime unmarshal(final Date v) throws Exception {
		return v == null ? null : new DateTime(v.getTime());
	}

}
