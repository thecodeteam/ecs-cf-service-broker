package com.emc.ecs.management.sdk;

import static com.emc.ecs.management.sdk.Constants.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.NamespaceCreate;
import com.emc.ecs.management.sdk.model.NamespaceInfo;
import com.emc.ecs.management.sdk.model.NamespaceUpdate;

public final class NamespaceAction {

    private NamespaceAction() {}

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
	connection.handleRemoteCall(POST, uri,
		new NamespaceCreate(namespace, namespaceAdmins,
			replicationGroup));
    }

    public static void create(Connection connection,
	    NamespaceCreate createParam) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE);
	connection.handleRemoteCall(POST, uri, createParam);
    }

    public static void delete(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace, DEACTIVATE);
	connection.handleRemoteCall(POST, uri, null);
    }

    public static NamespaceInfo get(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace);
	Response response = connection.handleRemoteCall(GET, uri, null);
	return response.readEntity(NamespaceInfo.class);
    }

    public static void update(Connection connection, String namespace,
	    NamespaceUpdate updateParam) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace);
	connection.handleRemoteCall(PUT, uri, updateParam);
    }
}