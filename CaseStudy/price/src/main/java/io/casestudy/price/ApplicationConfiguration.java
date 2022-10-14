package io.casestudy.price;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;

import io.casestudy.price.cache.PriceCacheService;
import io.casestudy.price.exceptions.GeneralServiceException;
import io.casestudy.price.pricingrules.PricingService;
import io.casestudy.price.pricingrules.PricingServiceImpl;
import io.casestudy.price.services.PriceService;
import io.casestudy.price.services.PriceServiceImpl;
import io.casestudy.price.verticles.HttpServerVerticle;
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
import io.vertx.ext.web.validation.BadRequestException;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.RequestPredicateException;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.healthchecks.HealthCheckHandler;
import io.vertx.rxjava3.ext.healthchecks.HealthChecks;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.openapi.Operation;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ComponentScan(basePackages = { "io.casestudy.price" })
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
		retriever.configStream().toFlowable().subscribe();

		retriever.listen(change -> {
			JsonObject newConfig = change.getNewConfiguration();
			log.info("Configuration has changed {}", newConfig.encodePrettily());
			vertx.eventBus().publish("config-changed-channel", newConfig);

		});

		return retriever;
	}

	@Autowired
	@Bean
	@DependsOn("configRetriever")
	public PriceCacheService priceCacheService(ConfigRetriever configRetriever) {
		return new PriceCacheService(configRetriever);
	}

	/*
	 * The Price service, retrieves a Price from the cache
	 */
	@Bean
	@DependsOn("priceCacheService")
	public PriceService priceService(PriceCacheService priceCacheService) {
		return new PriceServiceImpl(priceCacheService);
	}
	
	/*
	 * The Pricing service, mocks the sources of Prices by Flight and Date
	 */
	@Bean
	public PricingService pricingService(PriceCacheService priceCacheService) {
		PricingService service = new PricingServiceImpl(priceCacheService);
		service.warmupCache();
		return service;
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
	public HttpServer httpServer(Vertx vertx, HttpServerOptions httpServerOptions, RouterBuilder routerBuilder,
			@Qualifier("priceHandler") Operation priceHandler, HealthChecks healthChecks) {

		Router router = routerBuilder.createRouter().errorHandler(400, routingContext -> {
			if (!Objects.isNull(routingContext.failure())) {
				log.error("Router failed with bad request {}", routingContext.failure().getMessage(),
						routingContext.failure());
			}
		}).errorHandler(500, routingContext -> {
			if (!Objects.isNull(routingContext.failure())) {
				if (routingContext.failure() instanceof BadRequestException
						|| routingContext.failure() instanceof ParameterProcessorException
						|| routingContext.failure() instanceof BodyProcessorException
						|| routingContext.failure() instanceof RequestPredicateException) {
					log.error("Router failed with bad request {}", routingContext.failure().getMessage(),
							routingContext.failure());
				} else {
					log.error("Router failed with internal server error {}", routingContext.failure().getMessage(),
							routingContext.failure());
				}
			}
		}).errorHandler(501, routingContext -> {
			if (!Objects.isNull(routingContext.failure())) {
				log.error("Router failed with {}", routingContext.failure().getMessage(), routingContext.failure());
			}

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
	public Supplier<List<AbstractVerticle>> verticlesSupplier(ConfigRetriever configRetriever, HttpServer server) {
		return () -> {
			List<AbstractVerticle> verticles = new ArrayList<>();
			verticles.add(new HttpServerVerticle(configRetriever, server));
			return verticles;
		};
	}

	/*
	 * Server health checks
	 */
	@Autowired
	@Bean
	public HealthChecks healthChecks(ConfigRetriever configRetriever, Vertx vertx) {
		return HealthChecks.create(vertx).register("/price", 5, future -> future.complete(Status.OK(new JsonObject())));
	}

	/*
	 * OpenAPI spec router
	 */
	@Autowired
	@Bean
	public RouterBuilder routerBuilder(Vertx vertx) {

		RouterBuilderOptions options = new RouterBuilderOptions().setOperationModelKey("operationModel")
				.setContractEndpoint("/contract");

		return RouterBuilder.rxCreate(vertx, System.getProperty(ApplicationConstants.API_CONFIG_DIR)).doOnError(t -> {
			throw new GeneralServiceException("Unable to initialise Flight service", t);

		}).timeout(1000, TimeUnit.MILLISECONDS).blockingGet().setOptions(options);
	}

	@Autowired
	@Bean(name = "priceHandler")
	public Operation priceHandler(RouterBuilder routerBuilder, PriceService priceService) {
		return routerBuilder.operation("getPrice").handler(routingContext -> {
			priceService.price().accept(routingContext);

		}).failureHandler(routingContext -> {
			routingContext.request().response().setStatusCode(500).end();

			throw new GeneralServiceException(String.format("%s failed %s", routingContext.request().absoluteURI(),
					routingContext.failure().getMessage()), routingContext.failure());
		});
	}
}
