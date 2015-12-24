package com.emc.ecs.serviceBroker.repository;

import org.junit.Before;

import com.emc.ecs.common.EcsActionTest;
import com.emc.ecs.serviceBroker.EcsManagementClientException;
import com.emc.ecs.serviceBroker.EcsManagementResourceNotFoundException;
import com.emc.ecs.serviceBroker.EcsService;

public class RepositoryTest extends EcsActionTest {

	protected EcsRepositoryCredentials creds;
	protected EcsService ecs;

	@Before
	public void setUp() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		creds = new EcsRepositoryCredentials("repository", "user", namespace,
				replicationGroup, "ecs-cf-broker-");
		if (repositoryEndpoint != null) creds.setEndpoint(repositoryEndpoint);
		ecs = new EcsService();
	}
}
