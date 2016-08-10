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

import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class Result<V, E> {

	private Result() {}

	public abstract boolean isSuccess();

	public abstract V getValue();

	public abstract E getError();

	static final class Success<V, E> extends Result<V, E> {
		private final V value;

		public Success(final V value) {
			Objects.requireNonNull(value);
			this.value = value;
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public E getError() {
			throw new NoSuchElementException("No error value. It is a programming error to call getError() without checking isSuccess()");
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final Success<?, ?> success = (Success<?, ?>) o;
			return Objects.equals(value, success.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("Success{");
			sb.append("value=").append(value);
			sb.append('}');
			return sb.toString();
		}
	}

	static final class Failure<V, E> extends Result<V, E> {
		private final E error;

		public Failure(final E error) {
			Objects.requireNonNull(error);
			this.error = error;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public V getValue() {
			throw new NoSuchElementException("No value. It is a programming error to call getValue() without checking isSuccess()");
		}

		@Override
		public E getError() {
			return error;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			final Failure<?, ?> failure = (Failure<?, ?>) o;
			return Objects.equals(error, failure.error);
		}

		@Override
		public int hashCode() {
			return Objects.hash(error);
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("Failure{");
			sb.append("error=").append(error);
			sb.append('}');
			return sb.toString();
		}
	}
}
