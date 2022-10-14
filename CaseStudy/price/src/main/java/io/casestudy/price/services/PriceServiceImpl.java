package io.casestudy.price.services;

import java.util.Objects;
import java.util.function.Consumer;

import io.casestudy.price.cache.PriceCacheService;
import io.casestudy.price.pricingrules.Price;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriceServiceImpl implements PriceService {

	private final PriceCacheService priceCacheService;

	public PriceServiceImpl(PriceCacheService priceCacheService) {
		this.priceCacheService = priceCacheService;
	}

	@Override
	public Consumer<RoutingContext> price() {
		return routingContext -> {

			log.info("Price FLIGHT[{}], DATE[{}]", routingContext.queryParams().get("flight"),
					routingContext.queryParams().get("date"));

			String key = routingContext.queryParams().get("flight").concat(routingContext.queryParams().get("date"));

			Price price = priceCacheService.get().apply(key);

			if (!Objects.isNull(price)) {
				routingContext.response().setStatusCode(200).end(price.toString());
			} else {
				routingContext.response().setStatusCode(204).end();
			}
		};
	}
}