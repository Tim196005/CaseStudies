package io.casestudy.flight.downstream;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.reactivex.rxjava3.core.Flowable;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlowService3 implements DownstreamService {

	private final String name;

	public SlowService3() {
		this.name = "SlowService3";
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Supplier<Flowable<JsonObject>> doSlowCall(int pause) {
		return () -> Flowable.<JsonObject>just(new JsonObject().put("service", name).put("elapsed", pause));
	}

	@Override
	public Flowable<JsonObject> call() throws Exception {
		int pause = pause(500, 800);
		log.info("Call {} pause {}", name, pause);
		return doSlowCall(pause).get().delay(pause, TimeUnit.MILLISECONDS);
	}

}
