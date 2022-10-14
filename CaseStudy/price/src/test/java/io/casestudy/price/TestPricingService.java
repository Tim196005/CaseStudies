package io.casestudy.price;

import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.casestudy.price.cache.PriceCacheService;
import io.casestudy.price.pricingrules.Price;
import io.casestudy.price.pricingrules.PricingService;
import io.casestudy.price.pricingrules.PricingServiceImpl;

@TestInstance(Lifecycle.PER_CLASS)
public class TestPricingService {

	PricingService service;

	@Mock
	BiFunction<String, Price, Price> put;

	@Mock
	PriceCacheService cacheService;

	Price price;

	@BeforeAll
	public void beforeAll() {
		MockitoAnnotations.openMocks(this);

		price = new Price("EK0001", "2022-10-13", 1000.00, new byte[1024]);

		service = new PricingServiceImpl(cacheService, 1, 1024);
	}

	/*
	 * Cache warmup should put at least 365267 entries in the cache
	 */
	@Test
	void warmupCacheTest() throws Throwable {
		Mockito.when(put.apply(Mockito.anyString(), Mockito.any(Price.class))).thenReturn(price);
		Mockito.when(cacheService.put()).thenReturn(put);

		service.warmupCache();

		Mockito.verify(put, Mockito.atLeast(182633));
	}
}
