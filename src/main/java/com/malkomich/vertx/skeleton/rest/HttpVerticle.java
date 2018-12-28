package com.malkomich.vertx.skeleton.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;

public class HttpVerticle extends AbstractVerticle {

    public static final String SERVICES_CONFIG = "servicesConfig";

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HttpVerticle.class);

    private static final int DEFAULT_VERTX_PORT = 8080;
    private static final String VERTX_PORT = "port";

    @Override
    public void start(final Future<Void> future) {
        final JsonObject servicesConfig = config().getJsonObject(SERVICES_CONFIG);
        final Router router = generateRouter(servicesConfig);
        startServer(router, future);
    }

    private Router generateRouter(final JsonObject servicesConfig) {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        servicesConfig.forEach(service ->
            addPostEndpointHandler(router, service.getKey(), event -> {
                final String busAddress = String.valueOf(service.getValue());
                final JsonObject request = new JsonObject().put("request", event.getBodyAsJson());
                final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("action", "execute");
                vertx.eventBus().send(busAddress, request, deliveryOptions);
            }));
        return router;
    }

    private void startServer(final Router router, final Future<Void> future) {
        final Integer port = config().getInteger(VERTX_PORT, DEFAULT_VERTX_PORT);
        vertx.createHttpServer().requestHandler(router::accept).listen(port, asyncResult -> {
            if (asyncResult.failed()) {
                log.error("Could not start a HTTP server", asyncResult.cause());
                future.fail(asyncResult.cause());
                return;
            }
            log.debug("HTTP server running on port {}", port);
            future.complete();
        });
    }

    private void addPostEndpointHandler(final Router router,
                                        final String path,
                                        final Handler<RoutingContext> handlerFunction) {
        router.post(path)
            .handler(handlerFunction)
            .consumes(Headers.FORMAT);
    }
}
