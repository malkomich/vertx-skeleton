package com.github.malkomich.vertx.rest;

import com.github.malkomich.vertx.VertxService;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.ext.web.RoutingContext;

public class RestService {

    private VertxService service;

    RestService(final VertxService service) {
        this.service = service;
    }

    public static RestServiceBuilder builder() {
        return new RestServiceBuilder();
    }

    public void execute(final RoutingContext context) {
        service.execute(context.getBodyAsJson(), asyncResult -> genericHandler(asyncResult, context));
    }

    private void genericHandler(final AsyncResult<Void> asyncResult, final RoutingContext context) {
        if (asyncResult.failed()) {
            final ReplyException exception = RestHandler.getReplyException(asyncResult.cause());
            RestHandler.errorResponse(exception, context);
            return;
        }
        RestHandler.successfulResponse(context);
    }

    public static class RestServiceBuilder {
        private VertxService service;

        RestServiceBuilder() {
        }

        public RestServiceBuilder service(final VertxService service) {
            this.service = service;
            return this;
        }

        public RestService build() {
            return new RestService(service);
        }

        public String toString() {
            return "RestService.RestServiceBuilder(service=" + this.service + ")";
        }
    }
}
