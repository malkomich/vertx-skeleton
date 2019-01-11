package com.github.malkomich.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface VertxService {

    VertxService execute(final JsonObject request, final Handler<AsyncResult<Void>> handler);

    void close();

    default Handler<AsyncResult<Void>> init(final Future<Void> future) {
        return future;
    }
}
