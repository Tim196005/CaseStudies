package io.casestudy.price.services;

import java.util.function.Consumer;

import io.vertx.rxjava3.ext.web.RoutingContext;

public interface PriceService {

	Consumer<RoutingContext> price();
	
}
