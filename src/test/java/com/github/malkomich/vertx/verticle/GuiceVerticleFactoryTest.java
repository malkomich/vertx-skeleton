package com.github.malkomich.vertx.verticle;

import com.google.inject.Injector;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import mockit.Deencapsulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;

import static com.github.malkomich.vertx.verticle.GuiceVerticleFactory.GUICE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class GuiceVerticleFactoryTest {

    private static final String VERTICLE_NAME = CustomVerticle.class.getName();

    @Mock private Injector injector;
    @Mock private CustomVerticle customVerticle;

    private ClassLoader classLoader;
    private GuiceVerticleFactory verticleFactory;

    @BeforeEach
    void setUp() {
        initMocks(this);
        verticleFactory = new GuiceVerticleFactory(injector);
        classLoader = ClassLoader.getSystemClassLoader();
    }

    @Test
    void prefix() {
        final String result = verticleFactory.prefix();

        assertEquals(GUICE_PREFIX, result);
    }

    @Test
    void createVerticleWithoutClassLoader() {
        Deencapsulation.invoke(VerticleFactory.class, "removePrefix", VERTICLE_NAME);

        final Executable errorExecutable = () -> verticleFactory.createVerticle(VERTICLE_NAME, null);

        assertThrows(NullPointerException.class, errorExecutable);
    }

    @Test
    void createVerticleSuccessful() throws Exception {
        Deencapsulation.invoke(VerticleFactory.class, "removePrefix", VERTICLE_NAME);
        when(injector.getInstance(CustomVerticle.class)).thenReturn(customVerticle);

        final Verticle result = verticleFactory.createVerticle(VERTICLE_NAME, classLoader);

        assertNotNull(result);
        assertTrue(result instanceof CustomVerticle);
    }

    static class CustomVerticle implements Verticle {
        @Override
        public Vertx getVertx() {
            return null;
        }

        @Override
        public void init(final Vertx vertx, final Context context) {

        }

        @Override
        public void start(final Future<Void> future) throws Exception {

        }

        @Override
        public void stop(final Future<Void> future) throws Exception {

        }
    }
}
