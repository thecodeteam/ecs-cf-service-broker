package com.emc.ecs.management.sdk.actions;

import com.emc.ecs.management.sdk.ManagementAPIConnection;
import com.emc.ecs.management.sdk.model.NFSExport;
import com.emc.ecs.management.sdk.model.NFSExportList;
import com.emc.ecs.management.sdk.model.NFSExportsOption;
import com.emc.ecs.servicebroker.exception.EcsManagementClientException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;

import static com.emc.ecs.management.sdk.ManagementAPIConstants.*;
import static javax.ws.rs.HttpMethod.*;

public class NFSExportAction {
    public static List<NFSExport> list(ManagementAPIConnection connection, String pathPrefix) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder()
                .segment(OBJECT, NFS, EXPORTS)
                .queryParam("pathprefix", pathPrefix);
        Response response = connection.remoteCall(GET, uri, null);
        NFSExportList exportList = response.readEntity(NFSExportList.class);
        return exportList.getExports();
    }

    public static void create(ManagementAPIConnection connection, String exportPath) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NFS, EXPORTS);
        NFSExportsOption opt = new NFSExportsOption("*", "rw,authsys,root=nobody");
        connection.remoteCall(POST, uri, new NFSExport(exportPath, Arrays.asList(opt)));
    }

    public static void delete(ManagementAPIConnection connection, int exportId) throws EcsManagementClientException {
        UriBuilder uri = connection.uriBuilder().segment(OBJECT, NFS, EXPORTS, String.valueOf(exportId));
        connection.remoteCall(DELETE, uri, null);
    }
}
