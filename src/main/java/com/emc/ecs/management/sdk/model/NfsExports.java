package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "object_nfs_export")
public class NfsExports {


    private String path;
    private String id;
    private List<NfsExportsOptions> exportOptions;

    @XmlElement(name = "export-options")
    public List<NfsExportsOptions> getExportOptions() {
        return exportOptions;
    }

    public void setExportOptions(List<NfsExportsOptions> exportOptions) {
        this.exportOptions = exportOptions;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
