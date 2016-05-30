package com.emc.ecs.management.sdk;

import static com.emc.ecs.management.sdk.Constants.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.model.NamespaceQuotaParam;
import com.emc.ecs.management.sdk.model.NamespaceQuotaDetails;

public class NamespaceQuotaAction {

    private NamespaceQuotaAction() {}

    public static void create(Connection connection, String namespace,
	    NamespaceQuotaParam createParam)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NAMESPACES,
		NAMESPACE, namespace, QUOTA);
	connection.handleRemoteCall(PUT, uri, createParam);
    }

    public static NamespaceQuotaDetails get(Connection connection,
	    String namespace) throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		NAMESPACES, NAMESPACE, namespace, QUOTA);
	Response response = connection.handleRemoteCall(GET, uri, null);
	return response.readEntity(NamespaceQuotaDetails.class);
    }

    public static void delete(Connection connection, String namespace)
	    throws EcsManagementClientException {
	UriBuilder uri = connection.getUriBuilder().segment(OBJECT,
		NAMESPACES, NAMESPACE, namespace, QUOTA);
	connection.handleRemoteCall(DELETE, uri, null);
    }
}
