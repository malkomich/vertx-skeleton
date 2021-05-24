package com.github.malkomich.vertx.rest.api;

import com.github.malkomich.vertx.VertxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiFactoryTest {

    public static final String YAML_PATH = "yamlPath";
    public static final String OPERATION_ID = "operationId";
    public static final String VERTICLE_ADDRESS = "verticleAddress";
    public static final Class<VertxService> SERVICE_CLASS = VertxService.class;

    private ApiFactory apiFactory;

    @BeforeEach
    void setup() {
        apiFactory = ApiFactory.builder()
                .operation(OPERATION_ID, VERTICLE_ADDRESS, SERVICE_CLASS)
                .yamlPath(YAML_PATH)
                .build();
    }

    @Test
    void yamlPath() {
        final String result = apiFactory.getYamlPath();

        assertEquals(YAML_PATH, result);
    }

    @Test
    void operationApiDefinitions() {
        final List<OperationApiDefinition> result = apiFactory.getOperationApiDefinitions();

        assertEquals(OPERATION_ID, result.get(0).getOperationId());
        assertEquals(VERTICLE_ADDRESS, result.get(0).getVerticleAddress());
        assertEquals(SERVICE_CLASS, result.get(0).getServiceClass());
    }
}
