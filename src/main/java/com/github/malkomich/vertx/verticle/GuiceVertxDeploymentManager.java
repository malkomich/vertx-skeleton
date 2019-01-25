package com.github.malkomich.vertx.verticle;

import com.google.common.base.Preconditions;
import com.github.malkomich.vertx.rest.HttpVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

public class GuiceVertxDeploymentManager {

    private static final int WORKER_POOL_SIZE = 4;
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GuiceVertxDeploymentManager.class);

    private final Vertx vertx;

    public GuiceVertxDeploymentManager(final Vertx vertx) {
        this.vertx = Preconditions.checkNotNull(vertx);
    }

    public Future<Void> deployHttpVerticle(final JsonObject config,
                                           final JsonObject httpServicesConfig) {
        final Class clazz = HttpVerticle.class;
        final Future<Void> done = Future.future();
        config.put(HttpVerticle.HTTP_CONFIG, httpServicesConfig);
        this.vertx.deployVerticle(getFullVerticleName(clazz), deploymentOptions(config, true), result -> {
            if (!result.succeeded()) {
                log.info("Failed to deploy verticle: {} {{}}", clazz.getSimpleName(), result.cause());
                done.fail(result.cause());
                return;
            }
            log.info("Successfully deployed verticle: {}", clazz.getSimpleName());
            done.complete();
        });
        return done;
    }

    public Future<Void> deployWorkerVerticle(final Class clazz, final JsonObject config) {
        final Future<Void> done = Future.future();
        Preconditions.checkNotNull(clazz);
        this.vertx.deployVerticle(getFullVerticleName(clazz), deploymentOptions(config, false), result -> {
            if (!result.succeeded()) {
                log.info("Failed to deploy verticle: " + clazz + result.cause());
                done.fail(result.cause());
                return;
            }
            log.info("Successfully deployed verticle: " + clazz);
            done.complete();
        });
        return done;
    }

    private static String getFullVerticleName(final Class verticleClazz) {
        return GuiceVerticleFactory.GUICE_PREFIX
                .concat(":")
                .concat(verticleClazz.getCanonicalName());
    }

    private DeploymentOptions deploymentOptions(final JsonObject config, final Boolean worker) {
        return new DeploymentOptions()
                .setConfig(config)
                .setInstances(Runtime.getRuntime().availableProcessors())
                .setWorker(worker)
                .setWorkerPoolSize(WORKER_POOL_SIZE);
    }
}
