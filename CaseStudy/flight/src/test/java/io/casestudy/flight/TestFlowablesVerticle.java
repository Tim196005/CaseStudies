package io.casestudy.flight;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.config.ConfigRetriever;
import io.vertx.rxjava3.core.http.HttpServer;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class TestFlowablesVerticle {

	String classPath;

	String verticleName;

	ConfigRetriever configRetriever;

	ApplicationConfiguration configuration;

	HttpServer server;

	JsonObject serverConfig;

	@BeforeAll
	void beforeAll(VertxTestContext testContext) throws Exception {
		MockitoAnnotations.openMocks(this);

		classPath = new ClassPathResource("server-config.json").getFile().getParent().concat("/");

		/*
		 * Temporarily sets Java environment variables, so that they are available via
		 * System.getenv("X")
		 * 
		 * (See: https://github.com/stefanbirkner/system-lambda )
		 */
		withEnvironmentVariable(ApplicationConstants.JAVA_CONFIG_DIR, classPath).execute(() -> {

			Assertions.assertEquals(classPath, System.getenv(ApplicationConstants.JAVA_CONFIG_DIR));

			System.setProperty(ApplicationConstants.API_CONFIG_DIR,
					System.getenv(ApplicationConstants.JAVA_CONFIG_DIR).concat("flight-service-open-api-spec.yaml"));

			System.setProperty(ApplicationConstants.BASE_DIR, System.getenv(ApplicationConstants.JAVA_CONFIG_DIR));

			System.setProperty("vertx.config", classPath.concat("server-config.json"));

			System.setProperty(ApplicationConstants.JAVA_CONFIG_DIR, classPath.concat("server-config.json"));

			System.setProperty("logging.config",
					"file:".concat(System.getenv(ApplicationConstants.JAVA_CONFIG_DIR)).concat("logback.xml"));

			configuration = new ApplicationConfiguration();

			serverConfig = configuration.serverConfig();

		});

		testContext.awaitCompletion(2, TimeUnit.SECONDS);
		testContext.completeNow();
	}

	@Test
	void verticleDeployed(VertxTestContext testContext) throws Throwable {
		
		testContext.completeNow();
	}
}
