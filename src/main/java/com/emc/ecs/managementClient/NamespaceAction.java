package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.NamespaceCreate;
import com.emc.ecs.managementClient.model.NamespaceInfo;
import com.emc.ecs.serviceBroker.EcsManagementClientException;

public final class NamespaceAction {

    private static final String OBJECT = "object";
    private static final String NAMESPACES = "namespaces";
    private static final String NAMESPACE = "namespace";

    private NamespaceAction() {
    }

    public static boolean exists(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace);
	return connection.existenceQuery(uri, null);
    }

    public static void create(Connection connection, String namespace,
	    String namespaceAdmins, String replicationGroup)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE);
	connection.handleRemoteCall("post", uri,
		new NamespaceCreate(namespace, namespaceAdmins,
			replicationGroup));
    }

    public static void delete(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace, "deactivate");
	connection.handleRemoteCall("post", uri, null);
    }

    public static NamespaceInfo get(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace);
	Response response = connection.handleRemoteCall("get", uri, null);
	return response.readEntity(NamespaceInfo.class);
    }
}