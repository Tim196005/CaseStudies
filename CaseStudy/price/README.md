# Case Study Flight Service

## Tech Choices

[Vertx][1] is a mature **Async / Reactive library** with bindings for Java, Kotlin and Groovy
and consistently outperforming othet similar libraries in the [TechEmpower Web Framework Benchmarks][2]

It is currently ranked **11th** in a list of **142** frameworks (Spring WebFlux is currently ranked **67th**).

[1]: https://vertx.io "Title"
[2]: https://www.techempower.com/benchmarks/#section=data-r21&test=composite "Title"


On startup a cache of 3GB of mocked price data is warmed.

A 3Gb cache of prices is created in the PricingService

Price for Flight and Date is return by the PriceService, fetch a vaue from the cache

### From the log file: ###

## Tech Stack

- **Java JDK1.8** (Dependency in test requires < JDK17)
- **Core Stack**, Spring Boot / Spring Framework
- **Reactive Framework**, Vert.x and RxJava3
- **API**, OpenAPI Spec (vertx-web-api-contract)
- **In Memory Cache**, Caffiene
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

`curl --location --request GET 'http://localhost:8069/price?date=2022-10-22&flight=EK0001'`

### /contract ( OpenAPI Contract /flights API defined in OpenAPI v3 Spec)

`curl --location --request GET 'localhost:8069/contract' --header 'content-type: application/json'`

### /health (Service health endpoint)

`curl --location --request GET 'localhost:8069/health --header 'content-type: application/json''`

### Postman Collection

`CaseStudy.postman_collection.json`

