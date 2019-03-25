package com.github.malkomich.vertx.boot;

import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNull;

class InitializationServiceTest {

    @Mock
    private Injector injector;

    private InitializationService service;

    @BeforeEach
    void setUp() {
        service = InitializationService.getInstance();
    }

    @Test
    void executeWithoutInjector() {
        service.setBootLoader((injector, vertx, handler) -> assertNull(injector));
        service.execute(null, Vertx.vertx(), Future.future());
    }

    @Test
    void executeWithoutVertx() {
        service.setBootLoader((injector, vertx, handler) -> assertNull(vertx));
        service.execute(injector, null, Future.future());
    }

    @Test
    void executeSuccessful() {
        service.setBootLoader((injector, vertx, handler) -> Future.succeededFuture());
        service.execute(injector, Vertx.vertx(), Future.future());
    }
}
