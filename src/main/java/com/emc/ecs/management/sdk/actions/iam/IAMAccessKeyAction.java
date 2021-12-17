package com.emc.ecs.management.sdk.actions.iam;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.iam.user.CreateAccessKeyResponse;
import com.emc.ecs.management.sdk.model.iam.user.IamAccessKey;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.IAM;
import static com.emc.ecs.management.sdk.actions.iam.IAMActionUtils.accountHeader;
import static javax.ws.rs.HttpMethod.POST;

public class IAMAccessKeyAction {
    private IAMAccessKeyAction() {
    }

    public static IamAccessKey create(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                .queryParam("Action", "CreateAccessKey")
                .queryParam("UserName", userName);

        Response response = connection.remoteCall(POST, uri, null, accountHeader(accountId));
        CreateAccessKeyResponse ret = response.readEntity(CreateAccessKeyResponse.class);
        return ret.getCreateAccessKeyResult().getAccessKey();
    }

    public static IamAccessKey delete(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                .queryParam("Action", "DeleteAccessKey")
                .queryParam("UserName", userName);

        Response response = connection.remoteCall(POST, uri, null, accountHeader(accountId));
        CreateAccessKeyResponse ret = response.readEntity(CreateAccessKeyResponse.class);
        return ret.getCreateAccessKeyResult().getAccessKey();
    }
}
