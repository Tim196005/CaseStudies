package io.casestudy.price.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.vertx.core.json.JsonObject;

public interface CacheService<K, V, T> {

	Logger log = LoggerFactory.getLogger(CacheService.class);

	Function<K, T> get();

	BiFunction<K, V, T> put();

	Consumer<K> evict();

	static Cache<Object, Object> cache(JsonObject cacheDefinition) {
		return Caffeine.newBuilder().initialCapacity(cacheDefinition.getInteger(CacheConstants.CACHE_INITIAL_CAPACITY))
				.maximumSize(cacheDefinition.getInteger(CacheConstants.CACHE_MAX_CAPACITY))
				.expireAfterAccess(cacheDefinition.getInteger(CacheConstants.CACHE_EXPIRY),
						TimeUnit.valueOf(cacheDefinition.getString(CacheConstants.CACHE_EXPIRY_TIMEUNIT)))
				.recordStats().removalListener((key, value, cause) -> {
					if (cause.wasEvicted()) {
						log.info("{} key={} removed", cacheDefinition.getString("name"), key);
					}
				}).evictionListener((key, value, cause) -> {
					if (cause.wasEvicted()) {
						log.info("{} key={} evicted", cacheDefinition.getString("name"), key);
					}
				}).build();
	}

}