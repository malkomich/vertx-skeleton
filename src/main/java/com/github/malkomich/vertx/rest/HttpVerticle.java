package com.github.malkomich.vertx.rest;

import com.github.malkomich.vertx.VertxService;
import com.google.common.base.Preconditions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.slf4j.Logger;

import java.util.Map;

@Deprecated
public class HttpVerticle extends AbstractVerticle {

    public static final String HTTP_CONFIG = "httpConfig";
    public static final String ADDRESS = "address";
    public static final String SERVICE_CLASS = "service";

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(HttpVerticle.class);
    private static final int DEFAULT_VERTX_PORT = 8080;
    private static final String VERTX_PORT = "port";

    @Override
    public void start(final Future<Void> future) {
        final JsonObject servicesConfig = config().getJsonObject(HTTP_CONFIG);
        final Router router = generateRouter(servicesConfig);
        startServer(router, future);
    }

    private Router generateRouter(final JsonObject config) {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        config.forEach(endpointConfig -> {
            final RestService restService = RestService.builder()
                .service(getService(endpointConfig))
                .build();
            addPostEndpointHandler(router, endpointConfig.getKey(), restService::execute);
        });
        return router;
    }

    private VertxService getService(final Map.Entry<String, Object> endpointConfig) {
        final JsonObject serviceConfig = JsonObject.mapFrom(endpointConfig.getValue());
        final Class<? extends VertxService> serviceClass = getServiceClass(serviceConfig);
        return new ServiceProxyBuilder(vertx)
                .setAddress(serviceConfig.getString(ADDRESS))
                .build(serviceClass);
    }

    private Class<? extends VertxService> getServiceClass(final JsonObject serviceConfig) {
        try {
            final Class clazz = Class.forName(serviceConfig.getString(SERVICE_CLASS));
            Preconditions.checkArgument(VertxService.class.isAssignableFrom(clazz));
            return clazz;
        } catch (final ClassNotFoundException error) {
            throw new VertxException("Service class not found", error);
        } catch (final IllegalArgumentException error) {
            throw new VertxException("Service must implement " + VertxService.class.getSimpleName(), error);
        }
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
