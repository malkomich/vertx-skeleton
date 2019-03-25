package com.github.malkomich.vertx.util;

import com.google.inject.Injector;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class InjectUtilsTest {

    private static final String ADDRESS = "address";

    @Mock
    private Injector injector;
    @Mock
    private Vertx vertx;

    private static final Class<InjectUtilsTest.CustomClass> CLAZZ = InjectUtilsTest.CustomClass.class;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void getInstanceWithoutInjector() {
        final Executable errorExecutable = () -> InjectUtils.getInstance(null, CLAZZ);

        assertThrows(NullPointerException.class, errorExecutable);
    }

    @Test
    void getInstanceSuccessful() {
        final Object object = new Object();
        when(injector.getInstance(any(Class.class))).thenReturn(object);

        final Object result = InjectUtils.getInstance(injector, CLAZZ);

        assertEquals(object, result);
    }

    @Test
    void getVerticleServiceWithoutVertx() {
        final Executable errorExecutable = () -> InjectUtils.getVerticleService(null, ADDRESS, CLAZZ);

        assertThrows(NullPointerException.class, errorExecutable);
    }

    @Test
    void getVerticleServiceWithoutAddress() {
        final Executable errorExecutable = () -> InjectUtils.getVerticleService(vertx, null, CLAZZ);

        assertThrows(NullPointerException.class, errorExecutable);
    }

    @Test
    void getVerticleServiceWithoutClazz() {
        final Executable errorExecutable = () -> InjectUtils.getVerticleService(vertx, ADDRESS, null);

        assertThrows(NullPointerException.class, errorExecutable);
    }

    @Test
    void getVerticleServiceClassNotInjectable() {
        final Executable errorExecutable = () -> InjectUtils.getVerticleService(vertx, ADDRESS, CLAZZ);

        assertThrows(IllegalStateException.class, errorExecutable);
    }

    static class CustomClass {
    }
}
