package com.emc.ecs.managementClient;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.NamespaceQuotaDetails;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.model.NamespaceQuotaParam;
import static com.emc.ecs.managementClient.Constants.*;

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
