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
package no.digipost.api.client.errorhandling;

import org.junit.Test;

import java.net.ConnectException;
import java.nio.file.DirectoryIteratorException;
import java.rmi.ConnectIOException;
import java.util.concurrent.CancellationException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ErrorTypeTest {

	@Test
    public void resolveErrorTypeFromException() {
		assertThat(ErrorType.resolve(new ConnectException()), is(ErrorType.CONNECTION_ERROR));
    }

	@Test
    public void resolveErrorTypeFromRootCause() {
		assertThat(ErrorType.resolve(new DirectoryIteratorException(new ConnectException())), is(ErrorType.CONNECTION_ERROR));
    }

	@Test
    public void fallbackToGeneralErrorOnUnknownRootCause() {
		assertThat(ErrorType.resolve(new CancellationException()), is(ErrorType.GENERAL_ERROR));
	    assertThat(ErrorType.resolve(new ConnectIOException(null, new CancellationException())), is(ErrorType.GENERAL_ERROR));
    }

	@Test
    public void resolveFromString() {
	    assertThat(ErrorType.resolve(ErrorType.PROBLEM_WITH_REQUEST.name()), is(ErrorType.PROBLEM_WITH_REQUEST));
    }

	@Test
    public void fallbackToGeneralErrorOnUnknownString() {
	    assertThat(ErrorType.resolve("I_AM_ERROR"), is(ErrorType.GENERAL_ERROR));
    }
}
