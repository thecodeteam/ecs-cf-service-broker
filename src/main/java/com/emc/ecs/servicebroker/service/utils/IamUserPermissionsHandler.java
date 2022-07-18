package com.emc.ecs.servicebroker.service.utils;

import java.util.List;

import static com.emc.ecs.servicebroker.model.Constants.FULL_CONTROL;
import static org.apache.commons.collections.ListUtils.isEqualList;

public class IamUserPermissionsHandler {
    public static String getPermissionsList(List<String> permissions) {

        if(permissions == null || isEqualList(FULL_CONTROL, permissions)) {
            return  "        \"s3:PutObject\",\n" +
                    "        \"s3:PutObjectAcl\",\n" +
                    "        \"s3:GetObject\",\n" +
                    "        \"s3:GetObjectAcl\",\n" +
                    "        \"s3:DeleteObject\"\n";
        }

        String result = "";
        int n = permissions.size();
        for(int i = 0; i < n; ++i) {
            result = result + "\n" +
            "        \"s3:" + permissions.get(i) + "\"";

            if(i != n - 1) {
                result = result + ",";
            }
        }

        return result;
    }
}
