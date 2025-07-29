/*
 * Copyright (C) Posten Bring AS
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
package no.digipost.api.client.internal.http.request.interceptor;

import org.junit.jupiter.api.Test;

import static no.digipost.api.client.internal.http.request.interceptor.UserAgent.DIGIPOST_USER_AGENT;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserAgentTest {

    @Test
    public void should_return_correct_user_agent() {
        assertThat(DIGIPOST_USER_AGENT, containsString("no.digipost - digipost-api-client-java"));
    }

}
