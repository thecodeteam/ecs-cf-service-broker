package com.emc.ecs.managementClient;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.managementClient.model.DataServiceReplicationGroup;
import com.emc.ecs.managementClient.model.DataServiceReplicationGroupList;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import static com.emc.ecs.managementClient.Constants.*;

public final class ReplicationGroupAction {

	private ReplicationGroupAction() {}

	public static List<DataServiceReplicationGroup> list(Connection connection)
			throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder().segment(VDC,
				DATA_SERVICE, VPOOLS);
		Response response = connection.handleRemoteCall(GET, uri, null);
		DataServiceReplicationGroupList rgList = response
				.readEntity(DataServiceReplicationGroupList.class);
		return rgList.getReplicationGroups();
	}

	public static DataServiceReplicationGroup get(Connection connection,
			String id) throws EcsManagementClientException,
					EcsManagementResourceNotFoundException {
		List<DataServiceReplicationGroup> repGroups = list(connection);
		Optional<DataServiceReplicationGroup> optionalRg = repGroups.stream()
				.filter(rg -> rg.getId().equals(id)).findFirst();
		try {
			return optionalRg.get();
		} catch (NoSuchElementException e) {
			throw new EcsManagementResourceNotFoundException(e.getMessage());
		}
	}

}