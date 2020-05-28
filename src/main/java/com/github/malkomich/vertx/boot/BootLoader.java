package com.github.malkomich.vertx.boot;

import com.google.inject.Injector;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public interface BootLoader {

    void initialize(final Injector injector,
                    final Vertx vertx,
                    final Handler<AsyncResult<?>> handler);
}
