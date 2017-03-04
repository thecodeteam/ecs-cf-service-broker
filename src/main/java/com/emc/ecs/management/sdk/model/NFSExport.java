package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "object_nfs_export")
public class NFSExport {


    private String path;
    private Integer id;
    private List<NFSExportsOption> exportOptions;

    public NFSExport() {
    }

    public NFSExport(String path, List<NFSExportsOption> exportOptions) {
        this.path = path;
        this.exportOptions = exportOptions;
    }

    @XmlElement(name = "export-options")
    public List<NFSExportsOption> getExportOptions() {
        return exportOptions;
    }

    public void setExportOptions(List<NFSExportsOption> exportOptions) {
        this.exportOptions = exportOptions;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
