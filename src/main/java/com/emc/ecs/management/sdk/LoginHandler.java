package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

public interface LoginHandler {
    void login() throws EcsManagementClientException;
    void logout() throws EcsManagementClientException;
    String getAuthToken();
}
