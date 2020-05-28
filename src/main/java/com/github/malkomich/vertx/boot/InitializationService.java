package com.github.malkomich.vertx.boot;

import com.google.inject.Injector;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class InitializationService {

    private static InitializationService initializationService;

    public static InitializationService getInstance() {
        if (initializationService == null) {
            initializationService = new InitializationService(defaultBootLoader());
        }
        return initializationService;
    }

    private static BootLoader defaultBootLoader() {
        return (injector, proxyBuilder, handler) -> handler.handle(Future.succeededFuture());
    }

    private BootLoader bootLoader;

    private InitializationService(final BootLoader bootLoader) {
        this.bootLoader = bootLoader;
    }

    public void execute(final Injector injector, final Vertx vertx, final Future<Void> future) {
        bootLoader.initialize(injector, vertx, onInitialized -> {
            if (onInitialized.succeeded()) {
                future.complete();
            } else {
                future.fail(onInitialized.cause());
            }
        });
    }

    public void setBootLoader(final BootLoader bootLoader) {
        this.bootLoader = bootLoader;
    }
}
