package com.emc.ecs.managementClient;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.model.BaseUrl;
import com.emc.ecs.serviceBroker.model.BaseUrlInfo;

public class BaseUrlActionTest extends EcsActionTest {
	
	@Before
	public void setUp() throws EcsManagementClientException {
		connection.login();
	}
	
	@After
	public void cleanup() throws EcsManagementClientException {
		connection.logout();
	}

	@Test
	public void testBaseUrlListAndGet() throws EcsManagementClientException {
		BaseUrl baseUrl = BaseUrlAction.list(connection).get(0);
		BaseUrlInfo baseUrlInfo = BaseUrlAction.get(connection, baseUrl.getId());
		assertTrue(baseUrlInfo.getBaseurl().equals(baseUrl));
		assertTrue(baseUrlInfo.getName().equals("DefaultBaseUrl"));
	}

}