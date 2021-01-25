package com.emc.ecs.servicebroker.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

public class ListServiceInstancesResponse {

    @JsonSerialize
    @JsonProperty("marker")
    private String marker;

    @JsonSerialize
    @JsonProperty("pageSize")
    private int pageSize;

    @JsonSerialize
    @JsonProperty("nextMarker")
    private String nextMarker;

    @JsonSerialize
    @JsonProperty("instances")
    private List<ServiceInstance> instances;

    public ListServiceInstancesResponse() {
        this.instances = new ArrayList<>();
    }

    public ListServiceInstancesResponse(List<ServiceInstance> instances) {
        this.instances = instances;
    }

    public String getMarker() {
        return marker;
    }

    public int getPageSize() {
        return pageSize;
    }
    public String getNextMarker() {
        return nextMarker;
    }

    public List<ServiceInstance> getInstances() {
        return instances;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    public void setInstances(List<ServiceInstance> instances) {
        this.instances = instances;
    }
}
