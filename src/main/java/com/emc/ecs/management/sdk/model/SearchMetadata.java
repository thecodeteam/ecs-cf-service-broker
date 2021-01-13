package com.emc.ecs.management.sdk.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

import static com.emc.ecs.servicebroker.model.Constants.*;

@XmlRootElement(name = "metadata")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchMetadata implements Comparable<SearchMetadata> {

    private String type;
    private String name;
    private String datatype;

    public SearchMetadata() {
        super();
    }

    public SearchMetadata(String type, String name, String datatype) {
        this.type = type;
        this.name = name;
        this.datatype = datatype;
    }

    public SearchMetadata(Map<String, String> metadata) {
        this.type = metadata.get(SEARCH_METADATA_TYPE);
        this.name = metadata.get(SEARCH_METADATA_NAME);
        this.datatype = metadata.get(SEARCH_METADATA_DATATYPE);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    @Override
    public String toString() {
        return "SearchMetadata{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", datatype='" + datatype + '\'' +
                '}';
    }

    @Override
    public int compareTo(SearchMetadata o) {
        return name.compareTo(o.getName());
    }
}
