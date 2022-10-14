package io.casestudy.flight;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApplicationLauncher {

	private final Vertx vertx;

	private final Supplier<List<AbstractVerticle>> verticlesSupplier;

	public ApplicationLauncher(Vertx vertx, Supplier<List<AbstractVerticle>> verticlesSupplier) {
		this.vertx = vertx;
		this.verticlesSupplier = verticlesSupplier;

	}

	public void launch() {
		log.info("Launching services");
		
		verticlesSupplier.get().forEach(verticle -> {
			
			log.info("Deploying verticle");
			vertx.rxDeployVerticle(verticle).subscribe();
			
		});
	}
}
