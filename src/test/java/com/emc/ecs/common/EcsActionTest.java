package com.emc.ecs.common;

import java.net.URL;

import com.emc.ecs.managementClient.Connection;

public abstract class EcsActionTest {
	protected URL certificate = getClass().getClassLoader().getResource("localhost.pem");
	protected Connection connection = new Connection("https://104.197.254.237:4443", "root", "ChangeMe", certificate);
	protected String namespace = "ns1";
	protected String baseUrl = "s3.marqing.com";
	protected String replicationGroup = "urn:storageos:ReplicationGroupInfo:b3672185-e441-495e-8032-4660cb2616a3:global";
}