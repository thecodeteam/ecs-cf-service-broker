package com.emc.ecs.management.sdk.model;


public class NFSExportsOption {

    private String security;
    private String host;

    public NFSExportsOption() {
    }

    public NFSExportsOption(String host, String security) {
        this.host = host;
        this.security = security;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

}
