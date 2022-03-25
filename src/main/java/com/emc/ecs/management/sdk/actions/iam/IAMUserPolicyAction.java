package com.emc.ecs.management.sdk.actions.iam;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.UriBuilder;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.IAM;
import static com.emc.ecs.management.sdk.actions.iam.IAMActionUtils.accountHeader;
import static javax.ws.rs.HttpMethod.POST;

public class IAMUserPolicyAction {
    public static void attach(ManagementAPIConnection connection, String userName, String policyArn, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "AttachUserPolicy")
                .queryParam("UserName", userName)
                .queryParam("PolicyArn", policyArn);

        IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
    }

    public static void detach(ManagementAPIConnection connection, String userName, String policyArn, String accountId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(IAM)
                //.segment("local")
                .queryParam("Action", "DetachUserPolicy")
                .queryParam("UserName", userName)
                .queryParam("PolicyArn", policyArn);

        IAMActionUtils.remoteCall(connection, POST, uri, null, accountHeader(accountId));
    }
}
