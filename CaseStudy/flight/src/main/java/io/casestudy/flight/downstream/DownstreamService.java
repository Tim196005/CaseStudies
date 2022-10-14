package io.casestudy.flight.downstream;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Flowable;
import io.vertx.core.json.JsonObject;

public interface DownstreamService extends Callable<Flowable<JsonObject>> {

	String name();

	Supplier<Flowable<JsonObject>> doSlowCall(int pause);

	default int pause(int low, int high) {
		Random rand = new Random();
		return rand.nextInt(high - low) + low;

	}
}
