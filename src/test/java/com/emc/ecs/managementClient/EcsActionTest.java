package com.emc.ecs.managementClient;

import java.net.URL;

public abstract class EcsActionTest {
	protected URL certificate = EcsActionTest.class.getClassLoader().getResource("localhost.pem");
	protected Connection connection = new Connection("https://8.34.215.78:4443", "root", "ChangeMe", certificate);
	protected String namespace = "ns1";
	protected String replicationGroup = "urn:storageos:ReplicationGroupInfo:d4fc7068-1051-49ee-841f-1102e44c841e:global";
}