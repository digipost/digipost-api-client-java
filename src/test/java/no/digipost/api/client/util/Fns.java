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

import no.motif.f.Fn;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class Fns {

	public static <T> Fn<Future<T>, T> getInt() { return new Fn<Future<T>, T>() {
	@Override public T $(Future<T> futureValue) {
		try {
	        return futureValue.get();
	    } catch (InterruptedException | ExecutionException e) {
	        throw new RuntimeException(e.getMessage(), e);
	    }
	}}; }

	public static <T> Fn<Callable<T>, Future<T>> submit(final ExecutorService executorService) {
		return new Fn<Callable<T>, Future<T>>() {
			@Override public Future<T> $(Callable<T> callable) {
				return executorService.submit(callable);
	        }};
	}

	private Fns() {}
}
