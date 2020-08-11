package com.emc.ecs.servicebroker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class EcsManagementClientException extends Exception {
    private static final long serialVersionUID = 1L;

    public EcsManagementClientException(String message) {
        super(message);
    }

    public EcsManagementClientException(Exception e) {
        super(e);
    }
}