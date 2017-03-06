package com.emc.ecs.cloudfoundry.broker.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class CatalogConfigTest {

    @Autowired
    private Catalog catalog;

    @Test
    public void testEcsBucket() {
        ServiceDefinition ecsBucketService = catalog.getServiceDefinitions()
                .get(1);
        testServiceDefinition(ecsBucketService,
                "f3cbab6a-5172-4ff1-a5c7-72990f0ce2aa", "ecs-bucket",
                "Elastic Cloud S3 Object Storage Bucket", true, true, null,
                null);
    }

    @Test
    public void testEcsBucketMetadata() {
        ServiceDefinition ecsBucketService = catalog.getServiceDefinitions()
                .get(1);
        Map<String, Object> metadata = ecsBucketService.getMetadata();
        testServiceDefinitionMetadata(metadata, "ecs-bucket",
                "http://www.emc.com/images/products/header-image-icon-ecs.png",
                "EMC Corporation", "https://community.emc.com/docs/DOC-45012",
                "http://www.emc.com/products-solutions/trial-software-download/ecs.htm");
    }

    @Test
    public void testEcsBucketPlans() {
        // Service Definition Plans
        ServiceDefinition service = catalog.getServiceDefinitions().get(1);
        List<Plan> ecsBucketPlans = service.getPlans();
        Plan plan0 = ecsBucketPlans.get(0);

        testPlan(plan0, "8e777d49-0a78-4cf4-810a-b5f5173b019d", "5gb",
                "Free Trial", new Double(0.0), "MONTHLY",
                Arrays.asList("Shared object storage", "5 GB Storage",
                        "S3 protocol and HDFS access"));

        Plan plan1 = ecsBucketPlans.get(1);
        testPlan(plan1, "89d20694-9ab0-4a98-bc6a-868d6d4ecf31", "unlimited",
                "Pay per GB for Month", new Double(0.03), "PER GB PER MONTH",
                Arrays.asList("Shared object storage", "Unlimited Storage",
                        "S3 protocol and HDFS access"));
    }

    private void testServiceDefinition(ServiceDefinition service, String id,
            String name, String description, boolean bindable,
            boolean updatable, List<String> requires, String dashboardUrl) {
        assertEquals(id, service.getId());
        assertEquals(name, service.getName());
        assertEquals(description, service.getDescription());
        assertEquals(bindable, service.isBindable());
        assertEquals(updatable, service.isPlanUpdateable());
        assertEquals(requires, service.getRequires());
        assertEquals(dashboardUrl, service.getDashboardClient());
    }

    private void testServiceDefinitionMetadata(Map<String, Object> metadata,
            String displayName, String imageUrl, String providerDisplayName,
            String documentationUrl, String supportUrl) {
        assertEquals(displayName, metadata.get("displayName"));
        assertEquals(imageUrl, metadata.get("imageUrl"));
        assertEquals(providerDisplayName, metadata.get("providerDisplayName"));
        assertEquals(documentationUrl, metadata.get("documentationUrl"));
        assertEquals(supportUrl, metadata.get("supportUrl"));
    }

    private void testPlan(Plan plan, String id, String name, String description,
                          Double usdCost, String unit, List<String> bullets) {
        assertEquals(id, plan.getId());
        assertEquals(name, plan.getName());
        assertEquals(description, plan.getDescription());

        Map<String, Object> metadata = plan.getMetadata();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> costs = (List<Map<String, Object>>) metadata
                .get("costs");
        Map<String, Object> cost = costs.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> amount = (Map<String, Object>) cost.get("amount");
        assertEquals(usdCost, amount.get("usd"));
        assertEquals(unit, cost.get("unit"));
        assertEquals(bullets, metadata.get("bullets"));
    }

}
