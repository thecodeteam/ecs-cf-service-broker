package com.emc.ecs.management.sdk;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.BaseUrlAction;
import com.emc.ecs.management.sdk.model.BaseUrl;
import com.emc.ecs.management.sdk.model.BaseUrlInfo;

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
	BaseUrl baseUrl1 = BaseUrlAction.list(connection).get(0);
	BaseUrl baseUrl2 = BaseUrlAction.list(connection).get(1);
	BaseUrlInfo baseUrlInfo1 = BaseUrlAction.get(connection,
		baseUrl1.getId());
	BaseUrlInfo baseUrlInfo2 = BaseUrlAction.get(connection,
		baseUrl2.getId());
	assertTrue(baseUrlInfo1.getBaseurl().equals("localhost"));
	assertTrue(baseUrlInfo1.getName().equals("DefaultBaseUrl"));
	assertTrue(baseUrlInfo1.getNamespaceUrl(namespace, false)
		.equals("http://localhost:9020"));
	assertTrue(baseUrlInfo1.getNamespaceUrl(namespace, true)
		.equals("https://localhost:9021"));
	assertTrue(baseUrlInfo2.getBaseurl().equals("s3.10.5.5.5.xip.io"));
	assertTrue(baseUrlInfo2.getName().equals("xip.io"));
	assertTrue(baseUrlInfo2.getNamespaceUrl(namespace, false)
		.equals("http://ns1.s3.10.5.5.5.xip.io:9020"));
	assertTrue(baseUrlInfo2.getNamespaceUrl(namespace, true)
		.equals("https://ns1.s3.10.5.5.5.xip.io:9021"));
    }
}