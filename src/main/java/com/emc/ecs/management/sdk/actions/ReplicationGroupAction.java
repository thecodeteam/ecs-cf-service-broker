package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.DataServiceReplicationGroup;
import com.emc.ecs.management.sdk.model.DataServiceReplicationGroupList;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.GET;

public final class ReplicationGroupAction {

    private ReplicationGroupAction() {
    }

    public static List<DataServiceReplicationGroup> list(ManagementAPIConnection connection) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(VDC, DATA_SERVICE, VPOOLS);
        Response response = connection.remoteCall(GET, uri, null);
        DataServiceReplicationGroupList rgList = response.readEntity(DataServiceReplicationGroupList.class);
        return rgList.getReplicationGroups();
    }

    public static DataServiceReplicationGroup get(ManagementAPIConnection connection, String id) throws EcsManagementClientException, EcsManagementResourceNotFoundException {
        List<DataServiceReplicationGroup> repGroups = list(connection);
        Optional<DataServiceReplicationGroup> optionalRg = repGroups.stream()
                .filter(rg -> rg.getId().equals(id))
                .findFirst();
        try {
            return optionalRg.get();
        } catch (NoSuchElementException e) {
            throw new EcsManagementResourceNotFoundException(e);
        }
    }
}