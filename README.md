# NoSql Datastore Redis like implementation

## Requirements

In order to execute this application in your machine you'll need to have the following software installed:

- [Java JDK 17](https://openjdk.java.net/projects/jdk/17/)

## Init the application
```
./gradlew
```

## Run the application
```
./gradlew bootRun
```
You'll see that the REST api is exposed in localhost:8080

## Execute tests:
```
./gradlew test
```

## Execute benchmark:
```
./gradlew benchmark
```

If you use the intellij IDEA (intellij ultimate or only goland) you can execute all the gradle commands directly with the IDE

## Frameworks and libraries used
- [Spring boot](https://spring.io/)
- [Gradle](https://docs.gradle.org/current/userguide/what_is_gradle.html)
- [JUnit5](https://junit.org/junit5/docs/current/user-guide/)
- [kotlinx coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [kotlinx benchmark](https://github.com/Kotlin/kotlinx-benchmark)
- ...

## Architecture overview:
- This application was developed using the hexagonal architecture tactical approach of the Domain Driven Design.


- As this application is not holding any specific business logic I just decided to make a tradeoff and not having domain layer (the persistence layer is implementing directly the application service)
If at some point we need to add any kind of cache, transactions...we can create the NoSqlDataStore class that will manage all of this complexity and also will access to the persistence layer through another interface created in the domain layer 

## Folder structure:
- The entry point of the application that is holding the REST API is in src/main/kotlin/NosqlDataStoreApplication.kt class.


- All the code of the application lives in the src folder. the code itself inside the "main" folder and the tests (both unit and integration) and the benchmarks
inside the "test" folder


- application: Here we find the interface of the KotlinNoSqlDataStore and the Application Exceptions that the service can throw


- infrastructure/persistence: Here we find the storage implementations. In this case we're using an inMemory implementation for the NoSqlDataStoreService
- in the infrastructure layer we can also find the specific services needed to give support to the specific implementations of the application (i.e. StartKeyExpireServiceRunner)


- ui: Here we place all the specific ways to expose our application layer (application/service/NoSqlDataStore). Now as we're exposing the nosql commands using a http rest server we can find the following controller:
    - ui/http/rest/controller/NoSqlResourceController.kt


## Technical decisions

- In a first iteration I solved the expiration of the keys using the [Timer.schedule](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.concurrent/java.util.-timer/schedule.html) approach
but after running some benchmarks I realized that this approach is very slow as the Timer is adding the tasks to a queue and consuming them sequentially
Because of this I decided to create an independent process that is checking every fixed amount of time if there is any key to expire.

- The KotlinNoSqlExpireKeyService is being executed by Spring Boot just after the application is correctly bootstraped using the CommandLineRunner "SetKeyExpirerServiceRunner"

## TODO

- Implement the missing rest endpoints in the ui layer
- Mock the KotlinNoSqlDataStoreExpireKeyService in the KotlinNoSqlDataStoreUnitTest and create a separate unit test for it
- Add Unit tests to the ui layer (Rest Controller)
- Review how the framework is managing the graceful shutdown of the application (mainly the threads and coroutines) if some error occurs (i.e. what happens with the key expirer if the rest api is shutdown)
- Add spring actuator in order to have healtcheck and some other observability in the application
- ..