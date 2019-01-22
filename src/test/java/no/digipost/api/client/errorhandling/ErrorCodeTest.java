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

import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.nio.file.DirectoryIteratorException;
import java.rmi.ConnectIOException;
import java.util.concurrent.CancellationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ErrorCodeTest {

    @Test
    public void resolveErrorTypeFromException() {
        assertThat(ErrorCode.resolve(new ConnectException()), is(ErrorCode.CONNECTION_ERROR));
    }

    @Test
    public void resolveErrorTypeFromRootCause() {
        assertThat(ErrorCode.resolve(new DirectoryIteratorException(new ConnectException())), is(ErrorCode.CONNECTION_ERROR));
    }

    @Test
    public void fallbackToGeneralErrorOnUnknownRootCause() {
        assertThat(ErrorCode.resolve(new CancellationException()), is(ErrorCode.GENERAL_ERROR));
        assertThat(ErrorCode.resolve(new ConnectIOException(null, new CancellationException())), is(ErrorCode.GENERAL_ERROR));
    }

    @Test
    public void resolveFromString() {
        assertThat(ErrorCode.resolve(ErrorCode.PROBLEM_WITH_REQUEST.name()), is(ErrorCode.PROBLEM_WITH_REQUEST));
    }

    @Test
    public void fallbackToGeneralErrorOnUnknownString() {
        assertThat(ErrorCode.resolve("I_AM_ERROR"), is(ErrorCode.GENERAL_ERROR));
    }
}
