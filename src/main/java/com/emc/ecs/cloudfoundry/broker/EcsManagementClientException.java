package com.emc.ecs.cloudfoundry.broker;

public class EcsManagementClientException extends Exception {
    private static final long serialVersionUID = 1L;

    public EcsManagementClientException(String message) {
	super(message);
    }

    public EcsManagementClientException(Exception e) {
	super(e);
    }
}