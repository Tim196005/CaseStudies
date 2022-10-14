package io.casestudy.flight.services;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public interface FlightService {

	Consumer<RoutingContext> flights();
	
	Consumer<RoutingContext> processResponses();

	Supplier<Flowable<JsonObject>>  orchestratedCalls();
}
