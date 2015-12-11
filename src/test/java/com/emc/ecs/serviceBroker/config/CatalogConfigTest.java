package com.emc.ecs.serviceBroker.config;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=CatalogConfig.class, loader=AnnotationConfigContextLoader.class)
public class CatalogConfigTest {
	
	@Autowired
	private Catalog catalog;
	
	@Test
	public void testEcsBucket() {
		ServiceDefinition ecsBucketService = catalog.getServiceDefinitions().get(0);
		testServiceDefinition(
				ecsBucketService,
				"f3cbab6a-5172-4ff1-a5c7-72990f0ce2aa",
				"ecs-bucket",
				"Elastic Cloud Object Storage Bucket",
				true,
				true,
				Collections.emptyList(),
				null);
	}

	@Test
	public void testEcsBucketMetadata() {
		ServiceDefinition ecsBucketService = catalog.getServiceDefinitions().get(0);
		Map<String, Object> metadata = ecsBucketService.getMetadata();
		testServiceDefinitionMetadata(
				metadata,
				"ecs-bucket",
				"http://www.emc.com/images/products/header-image-icon-ecs.png",
				"EMC Corporation", "https://community.emc.com/docs/DOC-45012",
				"http://www.emc.com/products-solutions/trial-software-download/ecs.htm");
	}
	
	@Test
	public void testEcsBucketPlans() {
		// Service Definition Plans
		ServiceDefinition service = catalog.getServiceDefinitions().get(0);
		List<Plan> ecsBucketPlans = service.getPlans();
		Plan plan = ecsBucketPlans.get(0);
		testPlan(plan, "8e777d49-0a78-4cf4-810a-b5f5173b019d", "5gb", "5 GB ECS Bucket Plan");
	}

	private void testPlan(Plan plan, String id, String name, String description) {
		assertEquals(id, plan.getId());
		assertEquals(name, plan.getName());
		assertEquals(description, plan.getDescription());
	}
	
	private void testServiceDefinition(ServiceDefinition service, String id, String name, String description,
			boolean bindable, boolean updatable, List<String> requires, String dashboardUrl) {
		assertEquals(id, service.getId());
		assertEquals(name, service.getName());
		assertEquals(description, service.getDescription());
		assertEquals(bindable, service.isBindable());
		assertEquals(updatable, service.isPlanUpdatable());
		assertEquals(requires, service.getRequires());
		assertEquals(dashboardUrl, service.getDashboardClient());
	}

	private void testServiceDefinitionMetadata(Map<String, Object>metadata, String displayName, String imageUrl,
			String providerDisplayName, String documentationUrl, String supportUrl) {
		assertEquals("ecs-bucket",
				metadata.get("displayName"));
		assertEquals("http://www.emc.com/images/products/header-image-icon-ecs.png",
				metadata.get("imageUrl"));
		assertEquals("EMC Corporation",
				metadata.get("providerDisplayName"));
		assertEquals("https://community.emc.com/docs/DOC-45012",
				metadata.get("documentationUrl"));
		assertEquals("http://www.emc.com/products-solutions/trial-software-download/ecs.htm",
				metadata.get("supportUrl"));

	}

}
