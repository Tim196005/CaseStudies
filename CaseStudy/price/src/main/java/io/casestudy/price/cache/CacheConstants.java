package io.casestudy.price.cache;

import io.vertx.core.json.pointer.JsonPointer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConstants {

	/*
	 * Cache definitions
	 */
	public static final String CACHE_NAME = "cache-name";
	public static final String CACHE_INITIAL_CAPACITY = "initial-capacity";
	public static final String CACHE_MAX_CAPACITY = "max-capacity";
	public static final String CACHE_EXPIRY_TIMEUNIT = "expiry-timeunit";
	public static final String CACHE_EXPIRY = "expiry-interval";
	
	public static final JsonPointer PRICE_CACHE_POINTER = JsonPointer.from("/cache/priceCache");
	
}
