package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.NamespaceQuotaDetails;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class NamespaceQuotaAction {

    private NamespaceQuotaAction() {
    }

    public static void create(Connection connection, String namespace,
	    int limit, int warn) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment("object",
		"namespaces", "namespace", namespace, "quota");
	connection.handleRemoteCall("put", uri,
		new NamespaceQuotaParam(namespace, limit, warn));
    }

    public static NamespaceQuotaDetails get(Connection connection,
	    String namespace) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment("object",
		"namespaces", "namespace", namespace, "quota");
	Response response = connection.handleRemoteCall("get", uri, null);
	return response.readEntity(NamespaceQuotaDetails.class);
    }

    public static void delete(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment("object",
		"namespaces", "namespace", namespace, "quota");
	connection.handleRemoteCall("delete", uri, null);
    }
}
