package com.github.malkomich.vertx.util;

import com.google.inject.Injector;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class InjectUtils {

    private InjectUtils() {}

    public static <T> T getInstance(final Injector injector, final Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    public static <T> T getVerticleService(final Vertx vertx,
                                           final String address,
                                           final Class<T> clazz) {
        final ServiceProxyBuilder serviceProxyBuilder = new ServiceProxyBuilder(vertx);
        return serviceProxyBuilder
                .setAddress(address)
                .build(clazz);
    }
}
