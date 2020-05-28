package com.github.malkomich.vertx.verticle;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(VertxExtension.class)
class GuiceVertxDeploymentManagerTest {

    private static final Class CLAZZ = Object.class;

    private GuiceVertxDeploymentManager guiceVertxDeploymentManager;

    @Mock
    private Vertx vertx;

    @BeforeEach
    void setUp() {
        initMocks(this);
        guiceVertxDeploymentManager = new GuiceVertxDeploymentManager(vertx);
    }

    @Test
    void deployVerticleWithoutClass() {
        final JsonObject config = new JsonObject();

        final Executable errorExecution = () -> guiceVertxDeploymentManager.deployVerticle(null, config);

        assertThrows(NullPointerException.class, errorExecution);
    }

    @Test
    void deployVerticleSuccessful() {
        final JsonObject config = new JsonObject();

        final Future<Void> result = guiceVertxDeploymentManager.deployVerticle(CLAZZ, config);

        assertNotNull(result);
    }
}
