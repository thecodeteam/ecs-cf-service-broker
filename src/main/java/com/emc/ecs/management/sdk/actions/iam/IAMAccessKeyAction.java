package com.emc.ecs.management.sdk.actions.iam;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.iam.user.CreateAccessKeyResponse;
import com.emc.ecs.management.sdk.model.iam.user.IamAccessKey;
import com.emc.ecs.management.sdk.model.iam.user.ListAccessKeysResponse;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.List;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.IAM;
import static com.emc.ecs.management.sdk.actions.iam.IAMActionUtils.accountHeader;
import static javax.ws.rs.HttpMethod.POST;

public class IAMAccessKeyAction {
    private IAMAccessKeyAction() {
    }

    public static IamAccessKey create(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "CreateAccessKey")
                .queryParam("UserName", userName);

        Response response = IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
        CreateAccessKeyResponse ret = response.readEntity(CreateAccessKeyResponse.class);
        return ret.getCreateAccessKeyResult().getAccessKey();
    }

    public static List<IamAccessKey> list(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "ListAccessKeys")
                .queryParam("UserName", userName);

        Response response = IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
        if (response.getStatus() == 404) {
            return Collections.emptyList();
        }
        ListAccessKeysResponse ret = response.readEntity(ListAccessKeysResponse.class);
        return ret.getListAccessKeysResult().getAccessKeys();
    }

    public static void delete(ManagementAPIConnection connection, String accessKeyId, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "DeleteAccessKey")
                .queryParam("UserName", userName)
                .queryParam("AccessKeyId", accessKeyId);

        IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
    }
}
