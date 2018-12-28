package com.malkomich.vertx.skeleton.verticle;

import com.malkomich.vertx.skeleton.VertxService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.Arrays;

public abstract class ServiceVerticle<T extends VertxService> extends AbstractVerticle {

    private MessageConsumer binder;
    private T service;

    @Override
    public void init(final Vertx vertx, final Context context) {
        super.init(vertx, context);

        try {
            service = initializeService();
        } catch (final IllegalStateException error) {
            throw new VertxException("Error initializing verticle service", error);
        }
    }

    @Override
    public void start(final Future<Void> startFuture) {
        binder = new ServiceBinder(vertx)
                .setAddress(eventBusAddress())
                .register(serviceClass(), service);

        binder.completionHandler(startFuture);
    }

    @Override
    public void stop(final Future<Void> stopFuture) {
        service.close();
        binder.unregister(stopFuture);
    }

    protected abstract T initializeService();

    protected abstract String eventBusAddress();

    private Class serviceClass() {
        return Arrays.stream(service.getClass().getInterfaces())
                .filter(VertxService.class::isAssignableFrom)
                .findFirst()
                .orElseThrow(() -> new VertxException("Service class cannot be found"));
    }
}
