package com.emc.ecs.management.sdk;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.emc.ecs.common.Fixtures.*;
import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.ReplicationGroupAction;
import com.emc.ecs.management.sdk.model.DataServiceReplicationGroup;

public class ReplicationGroupActionTest extends EcsActionTest {

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
	List<DataServiceReplicationGroup> rgList = ReplicationGroupAction
		.list(connection);
	assertTrue(rgList.size() == 1);
    }

    @Test
    public void getReplicationGroup() throws EcsManagementClientException,
	    EcsManagementResourceNotFoundException {
	DataServiceReplicationGroup rg = ReplicationGroupAction.get(connection,
		RG_ID);
	assertTrue(rg.getName().equals("rg1"));
	assertTrue(rg.getId().equals(RG_ID));
    }

}