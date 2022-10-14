package io.casestudy.price.cache;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;

import io.casestudy.price.pricingrules.Price;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.config.ConfigRetriever;

public class PriceCacheService implements CacheService<String, Price, Price> {

	private Cache<Object, Object> cache;

	public PriceCacheService(ConfigRetriever configRetriever) {

		configRetriever.getConfig().subscribe(config -> {
			JsonObject priceCacheConfig = (JsonObject) CacheConstants.PRICE_CACHE_POINTER.queryJson(config);

			this.cache = CacheService.cache(priceCacheConfig);
		});
	}

	@Override
	public Function<String, Price> get() {
		return key -> (Price) cache.getIfPresent(key);
	}

	@Override
	public BiFunction<String, Price, Price> put() {
		return (key, value) -> {
			cache.put(key, value);
			return value;
		};
	}

	@Override
	public Consumer<String> evict() {
		return key -> cache.invalidate(key);
	}

}
