package com.github.malkomich.vertx.rest.api;

import com.github.malkomich.vertx.VertxService;

public class OperationApiDefinition {

    private final String operationId;
    private final String verticleAddress;
    private final Class<? extends VertxService> serviceClass;

    private OperationApiDefinition(final String operationId,
                           final String verticleAddress,
                           final Class<? extends VertxService> serviceClass) {
        this.operationId = operationId;
        this.verticleAddress = verticleAddress;
        this.serviceClass = serviceClass;
    }

    public static OperationApiDefinitionBuilder builder() {
        return new OperationApiDefinitionBuilder();
    }

    String getOperationId() {
        return this.operationId;
    }

    String getVerticleAddress() {
        return this.verticleAddress;
    }

    Class<? extends VertxService> getServiceClass() {
        return this.serviceClass;
    }

    public static class OperationApiDefinitionBuilder {
        private String operationId;
        private String verticleAddress;
        private Class<? extends VertxService> serviceClass;

        OperationApiDefinitionBuilder() {
        }

        OperationApiDefinitionBuilder operationId(final String operationId) {
            this.operationId = operationId;
            return this;
        }

        OperationApiDefinitionBuilder verticleAddress(final String verticleAddress) {
            this.verticleAddress = verticleAddress;
            return this;
        }

        OperationApiDefinitionBuilder serviceClass(final Class<? extends VertxService> serviceClass) {
            this.serviceClass = serviceClass;
            return this;
        }

        public OperationApiDefinition build() {
            return new OperationApiDefinition(operationId, verticleAddress, serviceClass);
        }

        public String toString() {
            return "OperationApiDefinition.OperationApiDefinitionBuilder(operationId=" + this.operationId
                    + ", verticleAddress=" + this.verticleAddress
                    + ", serviceClass=" + this.serviceClass + ")";
        }
    }
}
