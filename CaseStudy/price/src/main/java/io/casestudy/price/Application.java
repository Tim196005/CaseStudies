package io.casestudy.price;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

	public static void main(String[] args) {

		try {
			System.setProperty("spring.config.name", "application");

			// Base Dir
			System.setProperty(ApplicationConstants.BASE_DIR, System.getenv(ApplicationConstants.BASE_DIR));

			// Config
			System.setProperty("spring.config.location", System.getenv(ApplicationConstants.JAVA_CONFIG_DIR));

			// OpenAPI spec
			System.setProperty(ApplicationConstants.API_CONFIG_DIR,
					System.getenv(ApplicationConstants.BASE_DIR).concat("openapi/price-service-open-api-spec.yaml"));

			System.setProperty("vertx.config",
					System.getenv(ApplicationConstants.JAVA_CONFIG_DIR).concat("server-config.json"));

			// Logging
			System.setProperty("logging.config",
					"file:".concat(System.getenv(ApplicationConstants.JAVA_CONFIG_DIR)).concat("logback.xml"));

			System.setProperty("vertx.logger-delegate-factory-class-name",
					"io.vertx.core.logging.SLF4JLogDelegateFactory");

			InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);

			new SpringApplicationBuilder().sources(ApplicationConfiguration.class)
					.listeners(new ApplicationPidFileWriter()).run(args);

		} catch (Throwable t) {
			log.error("Error starting application {}", t.getMessage(), t);
		}

	}
}
