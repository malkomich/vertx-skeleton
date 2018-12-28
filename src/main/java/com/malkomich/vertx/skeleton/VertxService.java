package com.malkomich.vertx.skeleton;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface VertxService {

    VertxService execute(final JsonObject request, final Handler<AsyncResult<Void>> handler);

    void close();
}
