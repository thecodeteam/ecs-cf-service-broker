package com.emc.ecs.management.sdk;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.BaseUrl;
import com.emc.ecs.management.sdk.model.BaseUrlInfo;
import com.emc.ecs.management.sdk.model.BaseUrlList;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static com.emc.ecs.management.sdk.Constants.*;

public final class BaseUrlAction {

    private BaseUrlAction() {
    }

    public static List<BaseUrl> list(Connection connection)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BASEURL);
        Response response = connection.handleRemoteCall(GET, uri, null);
        return response.readEntity(BaseUrlList.class).getBaseUrls();
    }

    public static BaseUrlInfo get(Connection connection, String id)
            throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, BASEURL,
                id);
        Response response = connection.handleRemoteCall(GET, uri, null);
        return response.readEntity(BaseUrlInfo.class);
    }

}