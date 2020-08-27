package com.emc.ecs.servicebroker;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

public class EcsManagementResourceNotFoundException extends EcsManagementClientException {
    private static final long serialVersionUID = 1L;

    public EcsManagementResourceNotFoundException(String message) {
        super(message);
    }

    public EcsManagementResourceNotFoundException(NoSuchElementException e) {
        super(e);
    }
}
