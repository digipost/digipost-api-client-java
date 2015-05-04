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
package no.digipost.api.client.cache;

import no.digipost.api.client.util.Fns;
import no.digipost.api.client.util.FreezedTime;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static no.digipost.api.client.cache.CacheConfig.expireAfterAccess;
import static no.motif.Iterate.on;
import static org.hamcrest.Matchers.*;
import static org.joda.time.Duration.*;
import static org.junit.Assert.assertThat;

public class CacheTest {

	@Rule
	public final FreezedTime time = new FreezedTime(1000);

	private final Callable<Integer> incrementingValue = new Callable<Integer>() {
		final AtomicInteger num = new AtomicInteger(0);
		@Override public Integer call() {
			return num.getAndIncrement();
        }};


    private final SingleCached<Integer> value = new SingleCached<>("CacheTest", incrementingValue, expireAfterAccess(standardSeconds(1)));

	@Test
	public void resolvesValueOnFirstAccess() {
		assertThat(value.get(), is(0));
	}

	@Test
	public void reusesCachedValueWhileAccessingWithinExpiryTime() throws Exception {
		assertThat(asList(value.get(), value.get(), value.get()), everyItem(is(0)));
		time.wait(millis(900));
		assertThat(value.get(), is(0));
		time.wait(millis(20));
		assertThat(value.get(), is(0));
		time.wait(standardSeconds(14));
		assertThat(asList(value.get(), value.get(), value.get()), contains(1, 1, 1));
	}

	@Test(timeout = 40000)
	public void threadSafety() throws InterruptedException {
		final int threadAmount = 300;
		ExecutorService threadpool = Executors.newFixedThreadPool(threadAmount);
		try {
			Callable<Integer> valueWhenIncreased = new Callable<Integer>() {
				@Override
                public Integer call() throws Exception {
					while (value.get() == 0);
					return value.get();
                }
			};
			List<Future<Integer>> values = on(valueWhenIncreased).repeat(threadAmount).map(Fns.<Integer>submit(threadpool)).collect();
			Thread.sleep(2000);
			time.wait(standardSeconds(3));
			assertThat(on(values).map(Fns.<Integer>getInt()), everyItem(is(1)));
		} finally {
			threadpool.shutdown();
			threadpool.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void invalidatingCache() {
		assertThat(value.get(), is(0));
		assertThat(value.get(), is(0));
		time.wait(standardMinutes(1));
		assertThat(value.get(), is(1));
		assertThat(value.get(), is(1));
		value.invalidate();
		assertThat(value.get(), is(2));
	}



}
