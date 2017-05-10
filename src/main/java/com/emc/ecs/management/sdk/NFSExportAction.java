package com.emc.ecs.management.sdk;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.management.sdk.model.NFSExport;
import com.emc.ecs.management.sdk.model.NFSExportList;
import com.emc.ecs.management.sdk.model.NFSExportsOption;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.List;

import static com.emc.ecs.management.sdk.Constants.*;

public class NFSExportAction {
    public static List<NFSExport> list(Connection connection, String pathPrefix) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, EXPORTS).queryParam("pathprefix", pathPrefix);
        Response response = connection.handleRemoteCall(GET, uri, null);
        NFSExportList exportList = response
                .readEntity(NFSExportList.class);
        return exportList.getExports();
    }

    public static void create(Connection connection, String exportPath) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, EXPORTS);
        NFSExportsOption opt = new NFSExportsOption("*","rw,sys,root=nobody");
        connection.handleRemoteCall(POST, uri, new NFSExport(exportPath, Arrays.asList(opt)));
    }

    public static void delete(Connection connection, int exportId) throws EcsManagementClientException {
        UriBuilder uri = connection.getUriBuilder().segment(OBJECT, NFS, EXPORTS, String.valueOf(exportId));
        connection.handleRemoteCall(DELETE, uri, null);
    }
}
