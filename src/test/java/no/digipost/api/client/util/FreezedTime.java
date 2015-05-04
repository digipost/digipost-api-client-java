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

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.ReadableDuration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public final class FreezedTime implements TestRule {

	private final long freezedTime;

	public FreezedTime(long timeMillis) {
		this.freezedTime = timeMillis;
	}

	public FreezedTime(DateTime time) {
		this(time.getMillis());
	}

	public FreezedTime() {
		this(DateTime.now());
	}

	public void wait(ReadableDuration duration) {
		waitMilliseconds(duration.getMillis());
	}

	public void waitMilliseconds(long millis) {
		DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis() + millis);
	}

	@Override
	public Statement apply(final Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				DateTimeUtils.setCurrentMillisFixed(freezedTime);
				try {
					base.evaluate();
				} finally {
					DateTimeUtils.setCurrentMillisSystem();
				}
			}
		};
	}



}
