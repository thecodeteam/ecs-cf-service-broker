package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.NamespaceQuotaDetails;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public class NamespaceQuotaAction {

    private static final String QUOTA = "quota";
    private static final String OBJECT = "object";
    private static final String NAMESPACE = "namespace";
    private static final String NAMESPACES = "namespaces";

    private NamespaceQuotaAction() {
    }

    public static void create(Connection connection, String namespace,
	    NamespaceQuotaParam createParam)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace, QUOTA);
	connection.handleRemoteCall("put", uri, createParam);
    }

    public static NamespaceQuotaDetails get(Connection connection,
	    String namespace) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		NAMESPACES, NAMESPACE, namespace, QUOTA);
	Response response = connection.handleRemoteCall("get", uri, null);
	return response.readEntity(NamespaceQuotaDetails.class);
    }

    public static void delete(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		NAMESPACES, NAMESPACE, namespace, QUOTA);
	connection.handleRemoteCall("delete", uri, null);
    }
}
