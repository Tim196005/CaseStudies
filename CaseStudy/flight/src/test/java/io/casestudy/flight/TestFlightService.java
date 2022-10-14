package io.casestudy.flight;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.casestudy.flight.services.FlightService;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class TestFlightService {

	FlightService service;
	
	@Test
	void testFlight(VertxTestContext testContext) throws Throwable {
		testContext.completeNow();
	}
}
