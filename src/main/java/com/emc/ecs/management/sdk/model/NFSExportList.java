package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "object_nfs_exports")
public class NFSExportList {

    private List<NFSExport> exports;
    private Boolean successful;
    private String marker;

    public List<NFSExport> getExports() {
        return exports;
    }

    public void setExports(List<NFSExport> exports) {
        this.exports = exports;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }
}
