package com.emc.ecs.managementClient;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.model.BaseUrl;
import com.emc.ecs.serviceBroker.model.BaseUrlInfo;
import com.emc.ecs.serviceBroker.model.BaseUrlList;

public class BaseUrlAction {
	
	public static List<BaseUrl> list(Connection connection) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "baseurl");
		Response response = connection.handleRemoteCall("get", uri, null);
		return response.readEntity(BaseUrlList.class).getBaseUrls();
	}
	
	public static BaseUrlInfo get(Connection connection, String id) throws EcsManagementClientException {
		UriBuilder uri = connection.getUriBuilder()
				.segment("object", "baseurl", id);
		Response response = connection.handleRemoteCall("get", uri, null);
		return response.readEntity(BaseUrlInfo.class);
	}

}