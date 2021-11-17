package com.emc.ecs.servicebroker.model;

import com.emc.ecs.servicebroker.Application;
import com.emc.ecs.servicebroker.config.CatalogConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.cloud.servicebroker.model.catalog.Plan;
import org.springframework.cloud.servicebroker.model.catalog.ServiceDefinition;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
public class ServiceDefinitionProxyTest {

    @Autowired
    private CatalogConfig catalog;

    @Test
    public void testUnproxy() {
        ServiceDefinitionProxy service = catalog
                .findServiceDefinition("f3cbab6a-5172-4ff1-a5c7-72990f0ce2aa");
        assertEquals(PlanProxy.class, service.getPlans().get(0).getClass());
        ServiceDefinition service2 = service.unproxy();
        assertEquals(Plan.class, service2.getPlans().get(0).getClass());
    }

    @Test
    public void testFindPlan() {
        ServiceDefinitionProxy service = catalog
                .findServiceDefinition("f3cbab6a-5172-4ff1-a5c7-72990f0ce2aa");
        PlanProxy plan = service.findPlan("8e777d49-0a78-4cf4-810a-b5f5173b019d");
        assertEquals("5gb", plan.getName());
        assertEquals("Free Trial", plan.getDescription());
    }
}
