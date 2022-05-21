# ![RealWorld Example App](logo.png)

> ### Spring boot + WebFlux (Router Function) + Spock codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.


This codebase was created to demonstrate a backend of a fully fledged fullstack application built with **Spring boot + WebFlux (Router Function) ** including CRUD operations, authentication, routing, pagination, and more.

We've gone to great lengths to adhere to the **Spring boot + WebFlux (Router Function) ** community style guides & best practices.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.


# How it works
It uses Java 17, preferably openJdk 17, and Spring Reactive Stack: WebFlux + Spring Data Reactive MongoDB.  
It provides ability to handle concurrency with a small number of threads and scale with fewer hardware resources, with functional developemnt approach.
- [WebFlux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html) \
    NOTE: spring boot 5 webflux uses [Reactor](https://projectreactor.io/docs/core/release/reference/) version 3
- [MongoDB Reactive](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.reactive)


## Database
It uses embedded MongoDB database for demonstration purposes.


## Basic approach
The quality & architecture of this Conduit implementation reflect something similar to an early stage startup's MVP: functionally complete & stable, but not unnecessarily over-engineered.


## Project structure
```
- api - web layer which contains router function (AppRounter) and handlers (note: spock unit tests).
- dto - non-persistence tier data structures
- persistence - includes entities, repositories and a support classes
- exceptions - exceptions and exception handlers.
- security - security settings.
- service - contains the business logics (note: spock unit tests).
- validation - custom validators and validation settings.
```
## Tests
1. Integration tests covers followings, 
- End to End api tests using test harness covers all the happy paths.
- Repository test on customized respository impl
- Security
2. Unit tests utilize spock framework to mock the scenarios that not easily repeatable by Integration test
- api handlers
- services


# Getting started
You need Java 17 installed.
```
./gradlew bootRun
```

To test that it works, open a browser tab at http://localhost:8080/api/tags .  
Alternatively, you can run
```
curl http://localhost:8080/api/tags
```

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

```
./gradlew test
```

# Help

Please fork and PR to improve the project.

# Credits

Thanks to project [Spring Boot + WebFlux + MongoDB](https://github.com/a-mountain/realworld-spring-webflux) from which 
this project adopted its code bases of the data model, with the Functional WebFlux Approach (Router Function).

