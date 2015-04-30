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

import java.io.InputStream;
import java.util.Properties;

public class UserAgent {

	private static final String DIGIPOST_USER_AGENT_FALLBACK = "Digipost API Client";

	public static final String DIGIPOST_USER_AGENT;

	static {

		String userAgentTmp;

		try (InputStream in = UserAgent.class.getResourceAsStream("user-agent.properties")) {
			Properties properties = new Properties();
			properties.load(in);
			userAgentTmp = properties.getProperty("user-agent", DIGIPOST_USER_AGENT_FALLBACK);

		} catch (Exception e) {
			userAgentTmp = DIGIPOST_USER_AGENT_FALLBACK;
		}

		DIGIPOST_USER_AGENT = userAgentTmp;

	}

}
