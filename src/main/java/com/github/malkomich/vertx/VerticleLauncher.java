package com.github.malkomich.vertx;

import com.github.malkomich.vertx.boot.BootLoader;
import com.github.malkomich.vertx.boot.InitializationService;
import com.github.malkomich.vertx.properties.PropertiesConfig;
import com.github.malkomich.vertx.properties.PropertiesConfigModule;
import com.github.malkomich.vertx.rest.api.ApiFactory;
import com.github.malkomich.vertx.rest.api.RouterFactory;
import com.github.malkomich.vertx.verticle.GuiceVerticleFactory;
import com.github.malkomich.vertx.verticle.GuiceVertxDeploymentManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.malkomich.vertx.rest.HttpVerticle.ADDRESS;
import static com.github.malkomich.vertx.rest.HttpVerticle.SERVICE_CLASS;

public class VerticleLauncher {

    private static final long BLOCK_THREAD_CHECK_INTERVAL = 60000L;

    private final List<Class<?>> configModuleClasses;
    private final List<Class<?>> verticleClasses;
    /**
     * @deprecated (use ApiFactory for REST services)
     */
    @Deprecated
    private final JsonObject httpServicesConfig;
    private final InitializationService initializationService;
    private final ApiFactory apiFactory;

    private Vertx vertx;

    /**
     * @deprecated (use constructor with ApiFactory)
     */
    @Deprecated
    VerticleLauncher(final List<Class<?>> configModuleClasses,
                     final List<Class<?>> verticleClasses,
                     final JsonObject httpServicesConfig,
                     final InitializationService initializationService) {
        this.configModuleClasses = configModuleClasses;
        this.verticleClasses = verticleClasses;
        this.httpServicesConfig = httpServicesConfig;
        this.initializationService = initializationService;
        this.apiFactory = null;
    }

    VerticleLauncher(final List<Class<?>> configModuleClasses,
                     final List<Class<?>> verticleClasses,
                     final InitializationService initializationService,
                     final ApiFactory apiFactory) {
        this.configModuleClasses = configModuleClasses;
        this.verticleClasses = verticleClasses;
        httpServicesConfig = new JsonObject();
        this.initializationService = initializationService;
        this.apiFactory = apiFactory;
    }

    public static VerticleLauncher.VerticleLauncherBuilder builder() {
        return new VerticleLauncherBuilder();
    }

    private void launch(final String configFileName) {
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

        compositeFuture(asyncResult.result(), injector, done);
    }

    private Injector dependencyModulesInjector(final JsonObject config) {
        final List<AbstractModule> configModules = configModuleClasses.stream()
                .filter(AbstractModule.class::isAssignableFrom)
                .map(Class::getConstructors)
                .flatMap(Arrays::stream)
                .filter(this::configModuleValidConstructor)
                .map(constructor -> instanceObject(constructor, config))
                .map(AbstractModule.class::cast)
                .collect(Collectors.toList());
        return Guice.createInjector(configModules);
    }

    private Object instanceObject(final Constructor<?> constructor,
                                  final JsonObject config) {
        try {
            return constructor.newInstance(vertx, config);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException error) {
            throw new IllegalArgumentException("Config module constructor is not accessible", error);
        }
    }

    private boolean configModuleValidConstructor(final Constructor<?> constructor) {
        return Arrays.equals(
            constructor.getParameterTypes(),
            new Class[]{Vertx.class, JsonObject.class});
    }

    private void compositeFuture(final JsonObject config,
                                 final Injector injector,
                                 final Future<Void> future) {
        generateCompositeFuture(config).setHandler(asyncResult -> {
            if (!asyncResult.succeeded()) {
                future.fail(asyncResult.cause());
                return;
            }
            initializationService.execute(injector, vertx, future);
        });
    }

    private CompositeFuture generateCompositeFuture(final JsonObject config) {
        final GuiceVertxDeploymentManager deploymentManager = new GuiceVertxDeploymentManager(vertx);

        final List<Future> verticleFutures = verticleClasses.stream()
                .map(clazz -> deploymentManager.deployVerticle(clazz, config))
                .collect(Collectors.toList());
        final Future<Void> webApiFuture = webApiFuture(config, deploymentManager);
        if (webApiFuture != null) {
            verticleFutures.add(webApiFuture);
        }
        return CompositeFuture.all(verticleFutures);
    }

    private Future<Void> webApiFuture(final JsonObject config,
                                      final GuiceVertxDeploymentManager deploymentManager) {
        if (apiFactory != null) {
            return RouterFactory.builder()
                    .vertx(vertx)
                    .config(config)
                    .apiFactory(apiFactory)
                    .execute();
        }
        if (!httpServicesConfig.isEmpty()) {
            return deploymentManager.deployHttpVerticle(config, httpServicesConfig);
        }
        return null;
    }

    public static class VerticleLauncherBuilder {

        private static final String DEFAULT_CONFIG_FILE_NAME = "config.json";

        private List<Class<?>> configModuleClasses;
        private List<Class<?>> verticleClasses;
        private JsonObject httpServicesConfig;
        private InitializationService initializationService;
        private String configFileName;
        private ApiFactory apiFactory;

        VerticleLauncherBuilder() {
            configModuleClasses = new ArrayList<>();
            verticleClasses = new ArrayList<>();
            httpServicesConfig = new JsonObject();
            initializationService = InitializationService.getInstance();
            configFileName = DEFAULT_CONFIG_FILE_NAME;
        }

        public VerticleLauncher.VerticleLauncherBuilder configModules(
                final Class<?> ...configModuleClasses
        ) {
            this.configModuleClasses = Arrays.asList(configModuleClasses);
            return this;
        }

        public VerticleLauncher.VerticleLauncherBuilder verticles(
                final Class<?> ...verticleClasses
        ) {
            this.verticleClasses = Arrays.asList(verticleClasses);
            return this;
        }

        /**
         * @deprecated (use ApiFactory for REST services)
         */
        @Deprecated
        public VerticleLauncherBuilder withPublicEndpoint(final String path,
                                                          final String eventBusAddress,
                                                          final Class<? extends VertxService> service) {

            final JsonObject pathConfig = new JsonObject()
                    .put(ADDRESS, eventBusAddress)
                    .put(SERVICE_CLASS, service.getCanonicalName());
            httpServicesConfig.put(path, pathConfig);
            return this;
        }

        public VerticleLauncherBuilder apiFactory(final ApiFactory apiFactory) {
            this.apiFactory = apiFactory;
            return this;
        }

        public VerticleLauncherBuilder bootLoader(final BootLoader bootLoader) {
            initializationService.setBootLoader(bootLoader);
            return this;
        }

        public VerticleLauncherBuilder configFileName(final String configFileName) {
            this.configFileName = configFileName;
            return this;
        }

        /**
         * @deprecated (use constructor with ApiFactory)
         */
        @Deprecated
        public void execute_old() {
            new VerticleLauncher(configModuleClasses, verticleClasses, httpServicesConfig, initializationService)
                    .launch(configFileName);
        }

        public void execute() {
            new VerticleLauncher(configModuleClasses, verticleClasses, initializationService, apiFactory)
                    .launch(configFileName);
        }

        public String toString() {
            return "VerticleLauncher.VerticleLauncherBuilder(configModuleClasses=" + this.configModuleClasses
                    + ", verticleClasses=" + this.verticleClasses
                    + ", httpServicesConfig=" + this.httpServicesConfig + ")";
        }
    }
}
