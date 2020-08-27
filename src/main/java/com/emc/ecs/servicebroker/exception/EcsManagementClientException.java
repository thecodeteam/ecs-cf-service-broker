package com.emc.ecs.servicebroker.exception;

import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class EcsManagementClientException extends ServiceBrokerException {
    private static final long serialVersionUID = 1L;

    public EcsManagementClientException(String message) {
        super(message);
    }

    public EcsManagementClientException(Exception e) {
        super(e);
    }

    public EcsManagementClientException(String message, Throwable cause) {
        super(message, cause);
    }
}