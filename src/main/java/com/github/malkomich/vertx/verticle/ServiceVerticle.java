package com.github.malkomich.vertx.verticle;

import com.github.malkomich.vertx.VertxService;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.Arrays;

public abstract class ServiceVerticle<T extends VertxService> extends AbstractVerticle {

    private MessageConsumer<JsonObject> binder;
    protected T service;

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

        binder.completionHandler(service.init(startFuture));
    }

    @Override
    public void stop(final Future<Void> stopFuture) {
        service.close();
        binder.unregister(stopFuture);
    }

    protected abstract T initializeService();

    protected abstract String eventBusAddress();

    private Class<T> serviceClass() {
        return Arrays.stream(service.getClass().getInterfaces())
                .filter(VertxService.class::isAssignableFrom)
                .findFirst()
                .map(clazz -> (Class<T>) clazz)
                .orElseThrow(() -> new VertxException("Service class cannot be found"));
    }
}
