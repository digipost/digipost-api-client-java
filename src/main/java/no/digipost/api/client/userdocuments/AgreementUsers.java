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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "agreement-users")
public class AgreementUsers {

	@XmlElement(name = "user-id")
	@XmlJavaTypeAdapter(UserIdXmlAdapter.class)
	private List<UserId> users;

	private AgreementUsers() {}

	public AgreementUsers(final List<UserId> users) {
		this.users = users;
	}

	public List<UserId> getUsers() {
		if (users == null) {
			users = new ArrayList<>();
		}
		return users;
	}

	@Override
	public String toString() {
		return users.toString();
	}
}
