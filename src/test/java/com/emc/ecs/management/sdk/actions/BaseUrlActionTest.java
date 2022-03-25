package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.actions.BaseUrlAction;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.management.sdk.model.BaseUrl;
import com.emc.ecs.management.sdk.model.BaseUrlInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class BaseUrlActionTest extends EcsActionTest {
    @Test
    public void testBaseUrlListAndGet() throws EcsManagementClientException {
        BaseUrl baseUrl1 = BaseUrlAction.list(connection).get(0);
        BaseUrl baseUrl2 = BaseUrlAction.list(connection).get(1);
        BaseUrlInfo baseUrlInfo1 = BaseUrlAction.get(connection,
                baseUrl1.getId());
        BaseUrlInfo baseUrlInfo2 = BaseUrlAction.get(connection,
                baseUrl2.getId());
        assertEquals("localhost", baseUrlInfo1.getBaseurl());
        assertEquals("DefaultBaseUrl", baseUrlInfo1.getName());
        assertEquals("http://localhost:9020", baseUrlInfo1.getNamespaceUrl(namespace, false));
        assertEquals("https://localhost:9021", baseUrlInfo1.getNamespaceUrl(namespace, true));
        assertEquals("s3.10.5.5.5.xip.io", baseUrlInfo2.getBaseurl());
        assertEquals("xip.io", baseUrlInfo2.getName());
        assertEquals("http://ns1.s3.10.5.5.5.xip.io:9020", baseUrlInfo2.getNamespaceUrl(namespace, false));
        assertEquals("https://ns1.s3.10.5.5.5.xip.io:9021", baseUrlInfo2.getNamespaceUrl(namespace, true));
        assertTrue(true);
    }
}