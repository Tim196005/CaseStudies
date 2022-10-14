package io.casestudy.flight.services;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.casestudy.flight.downstream.DownstreamService;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subscribers.DisposableSubscriber;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FlightServiceImpl implements FlightService {

	private final Supplier<Collection<DownstreamService>> services;

	@Autowired
	public FlightServiceImpl(Supplier<Collection<DownstreamService>> services) {
		this.services = services;
	}

	@Override
	public Consumer<RoutingContext> flights() {
		return routingContext -> processResponses().accept(routingContext);
	}

	@Override
	public Consumer<RoutingContext> processResponses() {
		return routingContext -> {

			log.info("Search ORIGIN[{}], DESTINATION[{}], DATE[{}]", routingContext.queryParams().get("origin"),
					routingContext.queryParams().get("destination"), routingContext.queryParams().get("date"));

			orchestratedCalls().get().subscribeWith(new DisposableSubscriber<JsonObject>() {
				@Override
				public void onStart() {
					request(1);
				}

				@Override
				public void onNext(JsonObject t) {
					log.info("Next {}", t.toString());
					request(1);
				}

				@Override
				public void onError(Throwable t) {
					log.error("Error {}", t.getMessage(), t);
				}

				@Override
				public void onComplete() {
					routingContext.response().end(flightResponse().apply(routingContext));
				}
			});
		};
	}

	Function<RoutingContext, String> flightResponse() {
		return routingContext -> new JsonArray().add(new JsonObject().put("flightNumber", "EX001").put("origin", "DXB")
				.put("destination", "LHR").put("date", "2023-01-21T07:15:21Z")).toString();
	}

	@Override
	public Supplier<Flowable<JsonObject>> orchestratedCalls() {
		return () -> Flowable.fromIterable(services.get()).flatMap(service -> service.call());
	}

}