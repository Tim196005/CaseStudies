package io.casestudy.flight;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;

import io.casestudy.flight.downstream.DownstreamService;
import io.casestudy.flight.downstream.SlowService1;
import io.casestudy.flight.downstream.SlowService2;
import io.casestudy.flight.downstream.SlowService3;
import io.casestudy.flight.downstream.SlowService4;
import io.casestudy.flight.downstream.SlowService5;
import io.casestudy.flight.exceptions.GeneralServiceException;
import io.casestudy.flight.services.FlightService;
import io.casestudy.flight.verticles.HttpServerVerticle;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.healthchecks.HealthCheckHandler;
import io.vertx.rxjava3.ext.healthchecks.HealthChecks;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ComponentScan(basePackages = { "io.casestudy.flight" })
public class ApplicationConfiguration {

	@EventListener
	public void applicationReady(ApplicationReadyEvent event) {
		ConfigurableApplicationContext context = event.getApplicationContext();
		context.getBean(ApplicationLauncher.class).launch();
		log.info("Application ready at {}", LocalDateTime.now());
	}

	public static String readFile(String fileName) throws IOException {
		return IOUtils.toString(new FileInputStream(new File(fileName)), StandardCharsets.UTF_8);
	}

	@Bean(name = "serverConfig")
	public JsonObject serverConfig() {
		try {
			String serverConfig = readFile(System.getProperty("vertx.config"));
			return new JsonObject(serverConfig);

		} catch (IOException e) {
			log.error("Unable to load initial configuration {}", e.getMessage(), e);
			throw new GeneralServiceException("Unable to load initial configuration", e);

		}
	}

	@Bean
	@Autowired
	public Vertx vertx(VertxOptions vertxOptions) {
		return Vertx.vertx(vertxOptions).exceptionHandler(t -> log.error("Unhandled exception {}", t.getMessage(), t));
	}

	@Bean
	@Autowired
	public VertxOptions vertxOptions(@Qualifier("serverConfig") JsonObject serverConfig) {
		JsonObject eventBusConfig = (JsonObject) ApplicationConstants.EVENTBUS_OPTIONS_POINTER.queryJson(serverConfig);
		JsonObject vertxConfig = (JsonObject) ApplicationConstants.VERTX_OPTIONS_POINTER.queryJson(serverConfig);
		return new VertxOptions(vertxConfig).setEventBusOptions(new EventBusOptions(eventBusConfig));
	}

	@Bean
	@Autowired
	public ConfigRetrieverOptions configRetrieverOptions(JsonObject serverConfig) {
		ConfigStoreOptions configBaseDir = new ConfigStoreOptions().setType(ApplicationConstants.DIRECTORY)
				.setConfig(new JsonObject().put(ApplicationConstants.CACHE, "true")
						.put("path", System.getenv(ApplicationConstants.JAVA_CONFIG_DIR))
						.put(ApplicationConstants.FILESETS, new JsonArray()
								.add(new JsonObject().put(ApplicationConstants.PATTERN, "*.json")
										.put(ApplicationConstants.FORMAT, "json"))
								.add(new JsonObject()
										.put(ApplicationConstants.PATTERN, ApplicationConstants.FILETYPE_PROPERTIES)
										.put(ApplicationConstants.FORMAT, ApplicationConstants.PROPERTIES))
								.add(new JsonObject().put(ApplicationConstants.PATTERN, "*.yml")
										.put(ApplicationConstants.FORMAT, "yaml"))
								.add(new JsonObject().put(ApplicationConstants.PATTERN, "*.yaml")
										.put(ApplicationConstants.FORMAT, "yaml"))));

		JsonObject configRetrieverConfig = (JsonObject) ApplicationConstants.CONFIGRETRIEVER_OPTIONS_POINTER
				.queryJson(serverConfig);

		return new ConfigRetrieverOptions(configRetrieverConfig).addStore(configBaseDir);
	}

	@Bean
	@Autowired
	public ConfigRetriever configRetriever(Vertx vertx, @Qualifier("serverConfig") JsonObject serverConfig) {

		ConfigRetrieverOptions configRetrieverOptions = configRetrieverOptions(serverConfig);
		ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions);

		retriever.listen(change -> {
			JsonObject newConfig = change.getNewConfiguration();
			log.info("Configuration has changed {}", newConfig.encodePrettily());
			vertx.eventBus().publish("config-changed-channel", newConfig);

		});

		return retriever;
	}

	@Bean
	@Autowired
	public HttpClientOptions httpClientOptions(@Qualifier("serverConfig") JsonObject serverConfig) {
		return new HttpClientOptions(
				(JsonObject) ApplicationConstants.HTTPCLIENT_OPTIONS_POINTER.queryJson(serverConfig))
				.setTryUseCompression(true).setKeepAliveTimeout(1000).setSslHandshakeTimeout(1000)
				.setConnectTimeout(1000).setSsl(true).setTrustAll(true).setSslHandshakeTimeout(2)
				.setSslHandshakeTimeoutUnit(TimeUnit.SECONDS).addEnabledSecureTransportProtocol("https");
	}

	@Bean(destroyMethod = "close")
	@Autowired
	public HttpClient httpClient(Vertx vertx, HttpClientOptions httpClientOptions) {
		return vertx.createHttpClient(httpClientOptions);
	}

	@Bean
	@Autowired
	public HttpServerOptions httpServerOptions(@Qualifier("serverConfig") JsonObject serverConfig) {
		JsonObject httpServerConfig = (JsonObject) ApplicationConstants.SERVER_OPTIONS_POINTER.queryJson(serverConfig);
		return new HttpServerOptions(httpServerConfig).setCompressionSupported(true);
	}

	@Bean
	@Autowired
	public HttpServer httpServer(Vertx vertx, HttpServerOptions httpServerOptions, HealthChecks healthChecks,
			FlightService flightService) {

		RouterBuilderOptions options = new RouterBuilderOptions().setOperationModelKey("operationModel")
				.setContractEndpoint("/contract").setMountNotImplementedHandler(true);

		RouterBuilder routerBuilder = RouterBuilder
				.rxCreate(vertx, System.getProperty(ApplicationConstants.API_CONFIG_DIR)).doOnError(t -> {
					throw new GeneralServiceException("Unable to initialise Flight service", t);

				}).timeout(1000, TimeUnit.MILLISECONDS).blockingGet().setOptions(options);

		routerBuilder.operation("getFlights").handler(routingContext -> {
			flightService.flights().accept(routingContext);

		}).failureHandler(routingContext -> {
			log.error("Flight service failed", routingContext.failure());
			routingContext.response().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).end();

		});

		Router router = routerBuilder.createRouter().errorHandler(400, routingContext -> {
			if (routingContext.failure() instanceof ParameterProcessorException) {
				routingContext.response().setStatusMessage(routingContext.failure().getMessage()).end();
			}
		}).errorHandler(501, routingContext -> {
			log.error("501 {}",
					routingContext.failure() == null ? "" : routingContext.failure().getClass().getSimpleName());
		});

		router.get("/health").handler(HealthCheckHandler.createWithHealthChecks(healthChecks));

		router.getRoutes().forEach(route -> {
			if (!Objects.isNull(route.getPath())) {
				log.info("Route {} -> {} {}", route.getName(), route.getPath(), route.methods().toString());
			}
		});

		return vertx.createHttpServer(httpServerOptions).requestHandler(router).exceptionHandler(t -> {
			log.error("Request failed {}", t.getMessage(), t);

		}).invalidRequestHandler(request -> {
			log.error("Request failed {}", request.absoluteURI());
			request.response().setStatusCode(400).end();

		});
	}

	@Autowired
	@Bean(name = "verticlesSupplier")
	public Supplier<List<AbstractVerticle>> verticlesSupplier(HttpServer server) {
		return () -> {
			List<AbstractVerticle> verticles = new ArrayList<>();
			verticles.add(new HttpServerVerticle(server));
			return verticles;
		};
	}

	/*
	 * Server health checks
	 */
	@Autowired
	@Bean
	public HealthChecks healthChecks(Vertx vertx) {
		return HealthChecks.create(vertx).register("/flight", 5,
				future -> future.complete(Status.OK(new JsonObject())));
	}

	@Bean
	public Supplier<Collection<DownstreamService>> services() {
		return () -> Arrays.asList(new SlowService1(), new SlowService2(), new SlowService3(), new SlowService4(),
				new SlowService5());
	}

}
