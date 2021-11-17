package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class ObjectscaleMgmtAPIConnection extends AbstractManagementAPIConnection {
    public ObjectscaleMgmtAPIConnection(String endpoint, String username, String password, String certificate, boolean ignoreSslErrors) {
        super(endpoint, username, password, certificate, ignoreSslErrors);
    }

    @Override
    public void login() throws EcsManagementClientException {
        // TODO obtain token
    }

    @Override
    void logout() throws EcsManagementClientException {
        // TODO invalidate token
    }
}
