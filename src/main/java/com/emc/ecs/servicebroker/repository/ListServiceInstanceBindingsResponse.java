package com.emc.ecs.servicebroker.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

public class ListServiceInstanceBindingsResponse {

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
    @JsonProperty("bindings")
    private List<ServiceInstanceBinding> bindings;

    public ListServiceInstanceBindingsResponse() {
        this.bindings = new ArrayList<>();
    }

    public ListServiceInstanceBindingsResponse(List<ServiceInstanceBinding> bindings) {
        this.bindings = bindings;
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

    public List<ServiceInstanceBinding> getBindings() {
        return bindings;
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

    public void setBindings(List<ServiceInstanceBinding> bindings) {
        this.bindings = bindings;
    }
}
