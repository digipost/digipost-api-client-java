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

import no.digipost.api.client.representations.ErrorMessage;
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;

import java.io.IOException;
import java.util.Random;

import static co.unruly.matchers.Java8Matchers.where;
import static no.digipost.api.client.errorhandling.ErrorCode.BAD_CONTENT;
import static no.digipost.api.client.errorhandling.ErrorCode.CONTENT_OF_PRINT_MESSAGE_MUST_BE_PDF;
import static no.digipost.api.client.errorhandling.ErrorCode.FILE_TOO_LARGE;
import static no.digipost.api.client.errorhandling.ErrorCode.GENERAL_ERROR;
import static no.digipost.api.client.errorhandling.ErrorCode.ILLEGAL_ACCESS;
import static no.digipost.api.client.representations.ErrorType.CONFIGURATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DigipostClientExceptionTest implements WithQuickTheories {

    @Test
    public void resolvesToGeneralErrorWhenGivenUnknownErrorCode() {
        DigipostClientException exception = new DigipostClientException(new ErrorMessage(CONFIGURATION, "not-an-error-code", "fail message"));
        assertThat(exception.getErrorCode(), is(GENERAL_ERROR));
    }

    @Test
    public void checkIfContainedErrorCodeIsOneOfMany() {
        qt()
            .forAll(arrays().ofClass(arbitrary().enumValues(ErrorCode.class), ErrorCode.class).withLengthBetween(1, 20))
            .asWithPrecursor(randomErrors -> new DigipostClientException(randomErrors[new Random().nextInt(randomErrors.length)], "error"))
            .checkAssert((randomErrors, exceptionWithOneOfTheErrors) -> assertThat(randomErrors, where(exceptionWithOneOfTheErrors::isOneOf)));

        assertFalse(new DigipostClientException(BAD_CONTENT, "bad content").isOneOf(GENERAL_ERROR, CONTENT_OF_PRINT_MESSAGE_MUST_BE_PDF, ILLEGAL_ACCESS));
    }

    @Test
    public void getsMessageFromRootCause() {
        DigipostClientException tooLarge = new DigipostClientException(FILE_TOO_LARGE, new RuntimeException(new IOException("Too large!")));
        assertThat(tooLarge, where(DigipostClientException::getErrorMessage, containsString("Too large!")));
        assertThat(tooLarge, where(DigipostClientException::getMessage, containsString("Too large!")));
        assertThat(tooLarge, where(DigipostClientException::getErrorMessage, containsString(IOException.class.getSimpleName())));
        assertThat(tooLarge, where(DigipostClientException::getMessage, containsString(IOException.class.getSimpleName())));
    }
}
