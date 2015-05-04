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

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class CacheConfig implements ConfiguresGuavaCache {

	private static final Logger LOG = LoggerFactory.getLogger(CacheConfig.class);


	public static final CacheConfig useSoftValues = new CacheConfig() {
		@Override
		public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
			LOG.info("Using soft references for caching. See http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/ref/SoftReference.html");
			return builder.softValues();
		}
	};

	public static CacheConfig expireAfterAccess(final Duration expiryTime) {
		return new CacheConfig() {
			@Override
            public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
				LOG.info("Expires values {} ms after last access", expiryTime.getMillis());
				return builder.expireAfterAccess(expiryTime.getMillis(), TimeUnit.MILLISECONDS);
            }};
	}

	public static CacheConfig expireAfterWrite(final Duration expiryTime) {
		return new CacheConfig() {
			@Override
            public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
				LOG.info("Expire values {} ms after they are written to the cache", expiryTime.getMillis());
				return builder.expireAfterWrite(expiryTime.getMillis(), TimeUnit.MILLISECONDS);
            }};
	}

	public static CacheConfig initialCapacity(final int initCapacity) {
		return new CacheConfig() {
			@Override
			public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
				LOG.info("Initial capacity = {}" , initCapacity);
				return builder.initialCapacity(initCapacity);
			}};
	}

	public static CacheConfig maximumSize(final long size) {
		return new CacheConfig() {
			@Override
			public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
				LOG.info("Maximum size = {}", size);
				return builder.maximumSize(size);
			}};
	}



	static final CacheConfig jodaTicker = new CacheConfig() {
		@Override
		public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
			LOG.info("Using JodaTime as the clock source");
			return builder.ticker(new Ticker() {
				@Override
				public long read() {
					return DateTime.now().getMillis() * 1000000;
				}
			});
		}
	};

	static final CacheConfig logRemoval = new CacheConfig() {
		@Override
		public CacheBuilder<Object, Object> configure(CacheBuilder<Object, Object> builder) {
			return builder.removalListener(new RemovalListener<Object, Object>() {
				@Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
					Cache.LOG.info("Removing '{}' from cache, because {}.{}. (key='{}')", notification.getValue(), RemovalCause.class.getName(), notification.getCause(), notification.getKey());
                }
			});
		}
	};


	protected CacheConfig() {
	}
}
