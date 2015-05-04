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

import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static no.digipost.api.client.cache.CacheConfig.jodaTicker;
import static no.digipost.api.client.cache.CacheConfig.logRemoval;
import static no.motif.Iterate.on;

/**
 * Wrapper around {@link com.google.common.cache.Cache} from the Guava
 * library.
 */
public final class Cache<K, V> {

	static final Logger LOG = LoggerFactory.getLogger(Cache.class);

	private com.google.common.cache.Cache<K, V> guavaCache;
	private String name;

	public Cache(CacheConfig ... configurers) {
		this(asList(configurers));
	}

	public Cache(String name, CacheConfig ... configurers) {
		this(name, asList(configurers));
	}

	public Cache(Iterable<CacheConfig> configurers) {
		this("cache-" + UUID.randomUUID(), configurers);
	}

	public Cache(String name, Iterable<CacheConfig> configurers) {
		LOG.info("Creating new cache: {}", name);
		this.guavaCache = on(configurers).append(jodaTicker).append(logRemoval).reduce(CacheBuilder.newBuilder(), ConfiguresGuavaCache.applyConfiguration).build();
		this.name = name;
	}


	public V get(final K key, final Callable<V> valueResolver) {
		try {
	        return guavaCache.get(key, new Callable<V>() {
				@Override
                public V call() throws Exception {
					LOG.debug("{} resolving value for key {}", name, key);
					V value = valueResolver.call();
					LOG.info("Loaded '{}' into '{}' cache for key '{}'", value, name, key);
					return value;
                }
	        });
        } catch (ExecutionException e) {
	        throw new RuntimeException(e.getMessage(), e);
        }
	}

	public void invalidateAll() {
		LOG.debug("Invalidating all in {} cache", name);
		guavaCache.invalidateAll();
	}

	@SafeVarargs
    public final void invalidate(K ... keys) {
		invalidate(asList(keys));
	}

	public void invalidate(Iterable<? extends K> keys) {
		LOG.debug("Invalidating specific keys in {} cache", name);
		guavaCache.invalidateAll(keys);
	}

}
