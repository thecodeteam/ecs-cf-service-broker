package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.actions.ReplicationGroupAction;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.DataServiceReplicationGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.emc.ecs.common.Fixtures.RG_ID;
import static com.emc.ecs.common.Fixtures.RG_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReplicationGroupActionTest extends EcsActionTest {

    @Test
    public void listReplicationGroups() throws EcsManagementClientException {
        List<DataServiceReplicationGroup> rgList = ReplicationGroupAction
                .list(connection);
        assertEquals(1, rgList.size());
    }

    @Test
    public void getReplicationGroup() throws EcsManagementClientException {
        DataServiceReplicationGroup rg = ReplicationGroupAction.get(connection,
                RG_ID);
        assertEquals(rg.getName(), RG_NAME);
        assertEquals(rg.getId(), RG_ID);
    }

}