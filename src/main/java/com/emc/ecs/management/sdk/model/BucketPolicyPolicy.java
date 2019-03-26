package com.emc.ecs.management.sdk.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


public class BucketPolicyPolicy {

    public BucketPolicyPolicy() {
        super();
    }

    @JsonProperty("version")
    public String version = "2012-10-17";


}
