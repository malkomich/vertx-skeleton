package com.malkomich.vertx.skeleton;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.malkomich.vertx.skeleton.properties.PropertiesConfig;
import com.malkomich.vertx.skeleton.properties.PropertiesConfigModule;
import com.malkomich.vertx.skeleton.verticle.GuiceVerticleFactory;
import com.malkomich.vertx.skeleton.verticle.GuiceVertxDeploymentManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VerticleLauncher {
    private static final long BLOCK_THREAD_CHECK_INTERVAL = 60000L;
    private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";

    private final List<Class> configModuleClasses;
    private final List<Class> verticleClasses;
    private final JsonObject httpServicesConfig;

    private Vertx vertx;

    public VerticleLauncher(final List<Class> configModuleClasses,
                            final List<Class> verticleClasses,
                            final JsonObject httpServicesConfig) {
        this.configModuleClasses = configModuleClasses;
        this.verticleClasses = verticleClasses;
        this.httpServicesConfig = httpServicesConfig;
    }

    public void launch() {
        launch(DEFAULT_CONFIG_FILE_NAME);
    }

    public void launch(final String configFileName) {
        final VertxOptions options = new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCK_THREAD_CHECK_INTERVAL);
        vertx = Vertx.vertx(options);

        final PropertiesConfig propertiesConfig = Guice
                .createInjector(new PropertiesConfigModule(vertx, configFileName))
                .getInstance(PropertiesConfig.class);

        propertiesConfig.config(this::injectAndDeployVerticles);
    }

    private void injectAndDeployVerticles(final AsyncResult<JsonObject> asyncResult) {
        final Future<Void> done = Future.future();

        final Injector injector = dependencyModulesInjector(asyncResult.result());
        final GuiceVerticleFactory guiceVerticleFactory = new GuiceVerticleFactory(injector);
        vertx.registerVerticleFactory(guiceVerticleFactory);

        compositeFuture(asyncResult.result(), done);
    }

    private Injector dependencyModulesInjector(final JsonObject config) {
        final List<AbstractModule> configModules = configModuleClasses.stream()
                .filter(AbstractModule.class::isAssignableFrom)
                .map((Function<Class, Constructor[]>) Class::getConstructors)
                .flatMap(Arrays::stream)
                .filter(this::configModuleValidConstructor)
                .map(constructor -> instanceObject(constructor, config))
                .map(AbstractModule.class::cast)
                .collect(Collectors.toList());
        return Guice.createInjector(configModules);
    }

    private Object instanceObject(final Constructor constructor,
                                  final JsonObject config) {
        try {
            return constructor.newInstance(vertx, config);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException error) {
            throw new IllegalArgumentException("Config module constructor is not accessible", error);
        }
    }

    private boolean configModuleValidConstructor(final Constructor constructor) {
        return Arrays.equals(
            constructor.getParameterTypes(),
            new Class[]{Vertx.class, JsonObject.class});
    }

    private void compositeFuture(final JsonObject config, final Future<Void> done) {
        generateCompositeFuture(config).setHandler(asyncResult -> {
            if (!asyncResult.succeeded()) {
                done.fail(asyncResult.cause());
                return;
            }
            done.complete();
        });
    }

    private CompositeFuture generateCompositeFuture(final JsonObject config) {
        final GuiceVertxDeploymentManager deploymentManager = new GuiceVertxDeploymentManager(vertx);

        final List<Future> verticleFutures = verticleClasses.stream()
                .map(clazz -> deploymentManager.deployWorkerVerticle(clazz, config))
                .collect(Collectors.toList());
        if (!httpServicesConfig.isEmpty()) {
            final Future<Void> httpVerticleFuture = deploymentManager.deployHttpVerticle(config, httpServicesConfig);
            verticleFutures.add(httpVerticleFuture);
        }
        return CompositeFuture.all(verticleFutures);
    }
}
