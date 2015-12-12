package com.emc.ecs.managementClient;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.managementClient.model.DataServiceReplicationGroup;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;

public class ReplicationGroupActionTest extends EcsActionTest {
	private String replicationGroup = "rg1";

	@Before
	public void setUp() throws EcsManagementClientException {
		connection.login();
	}
	
	@After
	public void cleanup() throws EcsManagementClientException {
		connection.logout();
	}

	@Test
	public void listReplicationGroups() throws EcsManagementClientException {
		List<DataServiceReplicationGroup> rgList = ReplicationGroupAction.list(connection);
		assertTrue(rgList.size() == 1);
	}
	
	@Test
	public void getReplicationGroup() throws EcsManagementClientException, EcsManagementResourceNotFoundException {
		DataServiceReplicationGroup rg = ReplicationGroupAction.get(connection, replicationGroup);
		assertTrue(rg.getName().equals(replicationGroup));
	}

}