package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.BaseUrl;
import com.emc.ecs.management.sdk.model.BaseUrlInfo;
import com.emc.ecs.management.sdk.model.BaseUrlList;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.BASEURL;
import static com.emc.ecs.management.sdk.ManagementAPIConstants.OBJECT;

public final class BaseUrlAction {

    private BaseUrlAction() {
    }

    public static List<BaseUrl> list(ManagementAPIConnection connection) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BASEURL);
        Response response = connection.remoteCall(HttpMethod.GET, uri, null);
        return response.readEntity(BaseUrlList.class).getBaseUrls();
    }

    public static BaseUrlInfo get(ManagementAPIConnection connection, String id) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, BASEURL, id);
        Response response = connection.remoteCall(HttpMethod.GET, uri, null);
        return response.readEntity(BaseUrlInfo.class);
    }
}