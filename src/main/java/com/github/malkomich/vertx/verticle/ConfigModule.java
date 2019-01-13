package com.github.malkomich.vertx.verticle;

import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public abstract class ConfigModule extends AbstractModule {

    protected Vertx vertx;
    protected JsonObject config;

    public ConfigModule(final Vertx vertx, final JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }
}
