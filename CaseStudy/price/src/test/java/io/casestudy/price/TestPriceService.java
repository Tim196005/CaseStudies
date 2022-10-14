package io.casestudy.price;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.casestudy.price.cache.PriceCacheService;
import io.casestudy.price.pricingrules.Price;
import io.casestudy.price.services.PriceService;
import io.casestudy.price.services.PriceServiceImpl;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.http.HttpServerResponse;
import io.vertx.rxjava3.ext.web.RoutingContext;

@TestInstance(Lifecycle.PER_CLASS)
public class TestPriceService {

	PriceService service;

	@Mock
	Function<String, Price> get;

	@Mock
	PriceCacheService cacheService;

	@Mock
	RoutingContext routingContext;

	@Mock
	HttpServerResponse response;

	@Mock
	Completable completable;

	MultiMap queryParams;

	Price price;

	@BeforeAll
	public void beforeAll() {
		MockitoAnnotations.openMocks(this);

		queryParams = MultiMap.caseInsensitiveMultiMap();
		queryParams.add("flight", "EK0001");
		queryParams.add("date", "2022-10-13");

		price = new Price("EK0001", "2022-10-13", 1000.00, new byte[1024]);

		service = new PriceServiceImpl(cacheService);
	}

	@Test
	void testPrice() throws Throwable {
		Mockito.when(get.apply(Mockito.anyString())).thenReturn(price);
		Mockito.when(cacheService.get()).thenReturn(get);

		Mockito.when(routingContext.queryParams()).thenReturn(queryParams);

		Mockito.when(response.setStatusCode(Mockito.anyInt())).thenReturn(response);
		Mockito.when(response.end(Mockito.anyString())).thenReturn(completable);
		Mockito.when(routingContext.response()).thenReturn(response);

		service.price().accept(routingContext);

		Mockito.verify(response.end(Mockito.anyString()), Mockito.times(1));
	}
}
