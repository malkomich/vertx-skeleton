package com.github.malkomich.vertx.rest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;

class RestHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RestHandler.class);
    private static final String ERROR = "error";

    static void successfulResponse(final RoutingContext context,
                                   final JsonObject response) {
        final String message = (response != null) ? response.encode() : null;
        resolveContext(context, HttpResponseStatus.OK.code(), message);
    }

    static void successfulResponse(final RoutingContext context) {
        successfulResponse(context, null);
    }

    static void errorResponse(final ReplyException exception,
                              final RoutingContext context) {
        final JsonObject error = new JsonObject().put(ERROR, exception.getMessage());
        log.error(exception.getMessage());
        Integer status = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
        if (exception.failureCode() != -1) {
            status = exception.failureCode();
        }
        resolveContext(context, status, error.toString());
    }

    static void resolveContext(final RoutingContext context, final Integer status, final String message) {
        final HttpServerResponse httpServerResponse = context.response()
                .setStatusCode(status)
                .putHeader(Headers.CONTENT_TYPE, Headers.FORMAT)
                .putHeader(Headers.ACCEPT, Headers.FORMAT);
        if (message != null) {
            httpServerResponse.end(message);
            return;
        }
        httpServerResponse.end();
    }

    static ReplyException getReplyException(final Throwable throwable) {
        if (throwable instanceof ReplyException) {
            return (ReplyException) throwable;
        }
        return new ReplyException(ReplyFailure.NO_HANDLERS, throwable.getCause().getMessage());
    }
}
