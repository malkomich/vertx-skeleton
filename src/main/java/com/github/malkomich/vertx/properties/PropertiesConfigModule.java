package com.github.malkomich.vertx.properties;

import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;


public class PropertiesConfigModule extends AbstractModule {

    private final Vertx vertx;
    private final String configFileName;

    public PropertiesConfigModule(final Vertx vertx, final String configFileName) {
        this.vertx = vertx;
        this.configFileName = configFileName;
    }

    @Override
    protected void configure() {
        bind(PropertiesConfig.class).toInstance(propertyConfig());
    }

    private PropertiesConfig propertyConfig() {
        return new PropertiesConfig(vertx, configFileName);
    }
}
