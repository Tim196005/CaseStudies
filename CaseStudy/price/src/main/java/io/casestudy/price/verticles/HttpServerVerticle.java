package io.casestudy.price.verticles;

import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

	final String verticleName;

	final ConfigRetriever configRetriever;

	final HttpServer server;

	public HttpServerVerticle(ConfigRetriever configRetriever, HttpServer server) {
		this.verticleName = "flight-service-verticle";
		this.configRetriever = configRetriever;
		this.server = server;
	}

	@Override
	public Completable rxStart() {
		server.listen().subscribe();
		log.info("Server started on port {}", server.actualPort());
		return super.rxStart();

	}
}
