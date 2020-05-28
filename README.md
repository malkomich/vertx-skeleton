# Vertx Skeleton

[![Build Status](https://travis-ci.org/malkomich/vertx-skeleton.svg?branch=master)](https://travis-ci.org/malkomich/vertx-skeleton)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.malkomich%3Avertx-skeleton&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.malkomich%3Avertx-skeleton)


## Overview

Skeleton infrastructure classes for Vert.x projects.


## Requisites
* Java 8+
* Maven 3+


## Install

**Gradle**

```
	dependencies {
	    compile 'com.github.malkomich:vertx-skeleton:1.0.2'
	}
```

**Maven**

```
	<dependency>
	    <groupId>com.github.malkomich</groupId>
	    <artifactId>vertx-skeleton</artifactId>
	    <version>1.0.2</version>
	</dependency>
```


## Usage

##### CONFIG MODULE:
```java
public class CustomConfigModule extends ConfigModule {

    public CustomConfigModule(final Vertx vertx, final JsonObject config) {
        super(vertx, config);
    }

    @Override
    protected void configure() {
        bind(ThirdPartyService.class).toInstance(thirdPartyService());
    }

    private ThirdPartyService thirdPartyService() {
        return new ThirdPartyService();
    }
}
```

##### SERVICE:
```java
@ProxyGen
public interface CustomService extends VertxService {

    @Fluent
    @Override
    CustomService execute(final JsonObject json, final Handler<AsyncResult<Void>> handler);

    @ProxyClose
    @Override
    void close();
}

public class CustomServiceImpl implements CustomService {

    private ThirdPartyService thirdPartyService;

    public CustomServiceImpl(final ThirdPartyService thirdPartyService) {
        this.thirdPartyService = thirdPartyService;
    }


    @Override
    public CustomService execute(final JsonObject json,
                                 final Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture()); // TODO: Implement your custom service logic
        return this;
    }

    @Override
    public void close() {
        eventService.close(onClosed ->
            System.out.println("Service was closed"));
    }
}
```

##### VERTICLE:
```java
public class CustomVerticle extends ServiceVerticle<CustomService> {

    public static final String ADDRESS = "customAddress";

    @Inject
    private ThirdPartyService thirdPartyService;

    @Override
    protected CustomService initializeService() {
        return new CustomServiceImpl(thirdPartyService);
    }

    @Override
    protected String eventBusAddress() {
        return ADDRESS;
    }
}
```

##### LAUNCHER:
```java
public class ServiceLauncher {

    private static final String ENDPOINT_PATH = "/your-endpoint-path";

    public static void main(final String[] args) {
        new ServiceLauncher().launch();
    }

    private void launch() {
        VerticleLauncher.builder()
            .configModules(CustomConfigModule.class)
            .verticles(CustomVerticle.class) // Take care of verticles order (in case of dependency)
            .withPublicEndpoint(ENDPOINT_PATH, CustomVerticle.ADDRESS, CustomService.class)
            .execute();
    }
}
```


## License

[Apache License](http://www.apache.org/licenses/LICENSE-2.0.txt)

