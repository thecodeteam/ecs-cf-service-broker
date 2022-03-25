package com.emc.ecs.management.sdk.actions.iam;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.iam.user.CreateUserResponse;
import com.emc.ecs.management.sdk.model.iam.user.GetUserResponse;
import com.emc.ecs.management.sdk.model.iam.user.IamUser;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.IAM;
import static com.emc.ecs.management.sdk.actions.iam.IAMActionUtils.accountHeader;
import static javax.ws.rs.HttpMethod.POST;

public final class IAMUserAction {

    private IAMUserAction() {
    }

    public static IamUser create(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "CreateUser")
                .queryParam("UserName", userName);

        Response response = IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
        CreateUserResponse ret = response.readEntity(CreateUserResponse.class);
        return ret.getCreateUserResult().getUser();
    }

    public static IamUser get(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "GetUser")
                .queryParam("UserName", userName);

        Response response = IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
        GetUserResponse ret = response.readEntity(GetUserResponse.class);

        return ret.geUserResult().getUser();
    }

    public static boolean exists(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "GetUser")
                .queryParam("UserName", userName);

        return connection.existenceQuery(uri, null, accountHeader(accountId));
    }

    public static void delete(ManagementAPIConnection connection, String userName, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "DeleteUser")
                .queryParam("UserName", userName);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-emc-namespace", accountId);

        IAMActionUtils.remoteCall(connection, POST, uri, null, headers);
    }
}