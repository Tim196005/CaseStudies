# Case Study Flight Service

## Tech Choices

[Vertx][1] is a mature **Async / Reactive library** with bindings for Java, Kotlin and Groovy
and consistently outperforming othet similar libraries in the [TechEmpower Web Framework Benchmarks][2]

It is currently ranked **11th** in a list of **142** frameworks (Spring WebFlux is currently ranked **67th**).

[1]: https://vertx.io "Title"
[2]: https://www.techempower.com/benchmarks/#section=data-r21&test=composite "Title"

"Slow service responses" are mocked in services Service1..5 using a delayed "**Flowable**" with random delays of > 500ms and < 800ms.

These Flowables are "orchestrated" in the **FlightService** and are submitted asynchrously.

The "orchestrated" **Iterable Flowable** completes on completion of the "slowest" response from Services1..5 and the /flight endpoint returns a response.

### From the log file: ###

'
2022-10-13 12:31:07 [vert.x-eventloop-thread-3] INFO  i.c.flight.downstream.SlowService1 - Call SlowService1 pause 594

2022-10-13 12:31:07 [vert.x-eventloop-thread-3] INFO  i.c.flight.downstream.SlowService2 - Call SlowService2 pause 672

2022-10-13 12:31:07 [vert.x-eventloop-thread-3] INFO  i.c.flight.downstream.SlowService3 - Call SlowService3 pause 698

2022-10-13 12:31:07 [vert.x-eventloop-thread-3] INFO  i.c.flight.downstream.SlowService4 - Call SlowService4 pause 791

2022-10-13 12:31:07 [vert.x-eventloop-thread-3] INFO  i.c.flight.downstream.SlowService5 - Call SlowService5 pause 744

2022-10-13 12:31:08 [RxComputationThreadPool-2] INFO  i.c.f.services.FlightServiceImpl - Next {"service":"SlowService1","elapsed":594}

2022-10-13 12:31:08 [RxComputationThreadPool-3] INFO  i.c.f.services.FlightServiceImpl - Next {"service":"SlowService2","elapsed":672}

2022-10-13 12:31:08 [RxComputationThreadPool-4] INFO  i.c.f.services.FlightServiceImpl - Next {"service":"SlowService3","elapsed":698}

2022-10-13 12:31:08 [RxComputationThreadPool-6] INFO  i.c.f.services.FlightServiceImpl - Next {"service":"SlowService5","elapsed":744}

2022-10-13 12:31:08 [RxComputationThreadPool-5] INFO  i.c.f.services.FlightServiceImpl - Next {"service":"SlowService4","elapsed":791}
'

## Tech Stack

- **Java JDK1.8** (Dependency in test requires < JDK17)
- **Core Stack**, Spring Boot / Spring Framework
- **Reactive Framework**, Vert.x and RxJava3
- **API**, OpenAPI Spec (vertx-web-api-contract)
- **Others**, Lombok, Slf4j, Logback
- **Testing**, Junit5, Mockito, RestAssured
- **Build Tool**, Maven

## Checkout

``

## Build

`mvn clean install`

## Run from command line

`./scripts.run.sh`

## Eclipse Launcher

`Case Study Flight Service.launch`

## Service Endpoints

### /flight (Flight Case Study Endpoint with 5 Slow Service Async Calls)

`curl --location --request GET 'localhost:8079/flight?origin=DXB&destination=LHR&date=2022-10-31' --header 'Content-Type: application/json'`

### /contract ( OpenAPI Contract /flights API defined in OpenAPI v3 Spec)

`curl --location --request GET 'localhost:8079/contract' --header 'content-type: application/json'`

### /health (Service health endpoint)

`curl --location --request GET 'localhost:8079/health --header 'content-type: application/json''`

### Postman Collection

`CaseStudy.postman_collection.json`

