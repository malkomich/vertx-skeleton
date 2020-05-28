package com.github.malkomich.vertx.rest.api;

import com.github.malkomich.vertx.VertxService;

import java.util.ArrayList;
import java.util.List;

public class ApiFactory {

    private final String yamlPath;
    private final List<OperationApiDefinition> operationApiDefinitions;

    private ApiFactory(final String yamlPath, final List<OperationApiDefinition> operationApiDefinitions) {
        this.yamlPath = yamlPath;
        this.operationApiDefinitions = operationApiDefinitions;
    }

    public static ApiFactoryBuilder builder() {
        return new ApiFactoryBuilder();
    }

    String getYamlPath() {
        return this.yamlPath;
    }

    List<OperationApiDefinition> getOperationApiDefinitions() {
        return this.operationApiDefinitions;
    }

    public static class ApiFactoryBuilder {
        private final List<OperationApiDefinition> operations;
        private String yamlPath;

        ApiFactoryBuilder() {
            operations = new ArrayList<>();
        }

        public ApiFactoryBuilder yamlPath(final String yamlPath) {
            this.yamlPath = yamlPath;
            return this;
        }

        public ApiFactoryBuilder operation(final String operationId,
                                           final String verticleAddress,
                                           final Class<? extends VertxService> serviceClass) {
            final OperationApiDefinition apiDefinition = OperationApiDefinition.builder()
                    .operationId(operationId)
                    .verticleAddress(verticleAddress)
                    .serviceClass(serviceClass)
                    .build();
            operations.add(apiDefinition);
            return this;
        }

        public ApiFactory build() {
            return new ApiFactory(yamlPath, operations);
        }

        public String toString() {
            return "ApiFactory.ApiFactoryBuilder(yamlPath=" + this.yamlPath
                    + ", operations=" + this.operations + ")";
        }
    }
}
