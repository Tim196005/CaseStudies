package io.casestudy.flight;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.vertx.junit5.VertxExtension;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class TestBase {

	TestScheduler scheduler;
	
	@BeforeAll
	public void BeforeAll() {
		scheduler = new TestScheduler();
	}
}
