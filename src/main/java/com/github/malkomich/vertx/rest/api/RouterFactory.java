package com.github.malkomich.vertx.rest.api;

import com.github.malkomich.vertx.VertxService;
import com.github.malkomich.vertx.rest.RestService;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.serviceproxy.ServiceProxyBuilder;

import java.util.HashMap;
import java.util.Map;

public class RouterFactory {

    private static final String VERTX_PORT = "port";
    private static final int DEFAULT_VERTX_PORT = 8080;

    private final Vertx vertx;
    private final JsonObject config;
    private final String yamlPath;
    private final Map<String, Handler<RoutingContext>> operationSuccessHandlers;
    private final Map<String, Handler<RoutingContext>> operationFailHandlers;

    @SuppressWarnings("checkstyle:parameternumber")
    private RouterFactory(final Vertx vertx,
                          final JsonObject config,
                          final String yamlPath,
                          final Map<String, Handler<RoutingContext>> operationSuccessHandlers,
                          final Map<String, Handler<RoutingContext>> operationFailHandlers) {
        this.vertx = vertx;
        this.config = config;
        this.yamlPath = yamlPath;
        this.operationSuccessHandlers = operationSuccessHandlers;
        this.operationFailHandlers = operationFailHandlers;
    }

    public static RouterFactoryBuilder builder() {
        return new RouterFactoryBuilder();
    }

    public Future<Void> execute() {
        final Future<Void> future = Future.future();
        OpenAPI3RouterFactory.create(vertx, yamlPath, onRouterFactoryCreated -> {
            if (onRouterFactoryCreated.succeeded()) {
                final Router router = router(onRouterFactoryCreated.result());
                final HttpServer server = server();

                server.requestHandler(router).listen();
                future.complete();
            } else {
                future.fail(onRouterFactoryCreated.cause());
            }
        });
        return future;
    }

    private Router router(final OpenAPI3RouterFactory routerFactory) {
        routerFactory.setOptions(options());
        operationSuccessHandlers.forEach(routerFactory::addHandlerByOperationId);
        operationFailHandlers.forEach(routerFactory::addFailureHandlerByOperationId);

        return routerFactory.getRouter();
    }

    private HttpServer server() {
        final Integer port = config.getInteger(VERTX_PORT, DEFAULT_VERTX_PORT);

        final HttpServerOptions serverOptions = new HttpServerOptions()
                .setPort(port);
        return vertx.createHttpServer(serverOptions);
    }

    private RouterFactoryOptions options() {
        return new RouterFactoryOptions()
                .setMountNotImplementedHandler(true)
                .setMountValidationFailureHandler(false);
    }

    public static class RouterFactoryBuilder {
        private final Map<String, Handler<RoutingContext>> operationSuccessHandlers;
        private final Map<String, Handler<RoutingContext>> operationFailHandlers;
        private Vertx vertx;
        private JsonObject config;
        private String yamlPath;

        private RouterFactoryBuilder() {
            operationSuccessHandlers = new HashMap<>();
            operationFailHandlers = new HashMap<>();
        }

        public RouterFactoryBuilder vertx(final Vertx vertx) {
            this.vertx = vertx;
            return this;
        }

        public RouterFactoryBuilder config(final JsonObject config) {
            this.config = config;
            return this;
        }

        public RouterFactoryBuilder apiFactory(final ApiFactory apiFactory) {
            this.yamlPath = apiFactory.getYamlPath();
            apiFactory.getOperationApiDefinitions()
                    .forEach(this::newOperation);
            return this;
        }

        public RouterFactory build() {
            return new RouterFactory(vertx, config, yamlPath, operationSuccessHandlers, operationFailHandlers);
        }

        public Future<Void> execute() {
            return build().execute();
        }

        public String toString() {
            return "RouterFactory.RouterFactoryBuilder(vertx=" + this.vertx
                    + ", config=" + this.config
                    + ", yamlPath=" + this.yamlPath
                    + ", operationSuccessHandlers=" + this.operationSuccessHandlers
                    + ", operationFailHandlers=" + this.operationFailHandlers + ")";
        }

        private void newOperation(final OperationApiDefinition apiDefinition) {
            final RestService restService = RestService.builder()
                    .service(getService(apiDefinition.getServiceClass(), apiDefinition.getVerticleAddress()))
                    .build();
            operationSuccessHandlers.put(apiDefinition.getOperationId(), restService::execute);
            operationFailHandlers.put(apiDefinition.getOperationId(), defaultFailureHandler());
        }

        private Handler<RoutingContext> defaultFailureHandler() {
            return (routingContext) -> routingContext.fail(routingContext.failure());
        }

        private VertxService getService(final Class<? extends VertxService> clazz, final String address) {
            return new ServiceProxyBuilder(vertx)
                    .setAddress(address)
                    .build(clazz);
        }
    }
}
