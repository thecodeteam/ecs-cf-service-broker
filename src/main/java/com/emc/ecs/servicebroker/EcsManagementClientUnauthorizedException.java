package com.emc.ecs.servicebroker;

public class EcsManagementClientUnauthorizedException extends EcsManagementClientException {
    private static final long serialVersionUID = 1L;

    public EcsManagementClientUnauthorizedException(String message) {
        super(message);
    }

    public EcsManagementClientUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}