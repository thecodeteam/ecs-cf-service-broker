package com.emc.ecs.common;

import com.emc.ecs.management.sdk.Connection;

import java.net.URL;

public abstract class EcsActionTest {
    protected URL certificate = getClass().getClassLoader()
            .getResource("localhost.pem");
    protected Connection connection = new Connection("https://127.0.0.1:4443",
            "root", "ChangeMe", certificate);
    protected String namespace = "ns1";
    protected String baseUrlHost = "localhost";
    protected String replicationGroup =
            "urn:storageos:ReplicationGroupInfo:2ef0a92d-cf88-4933-90ba-90245aa031b1:global";
    protected String repositoryEndpoint;
}