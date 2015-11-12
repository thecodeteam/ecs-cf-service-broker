package com.emc.ecs.serviceBroker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;

@Configuration
public class CatalogConfig {
	
	@Bean
	public Catalog catalog() {
		ServiceDefinition bucketSvc = new ServiceDefinition(
				"ecs-bucket",
				"ecs bucket",
				"Elastic Cloud Object Storage Bucket",
				true,  // bindable
				true, // planUpdatable
				getBucketPlans(),
				Arrays.asList("object", "storage", "hadoop"),
				getServiceDefinitionMetadata(),
				null, // requires
				null  // dashboardClient
				);
		return new Catalog(Arrays.asList(bucketSvc));
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<String, Object>();
		sdMetadata.put("displaName", "ECS Bucket");
		sdMetadata.put("imageUrl", "http://www.emc.com/images/products/header-image-icon-ecs.png");
		sdMetadata.put("longDescription", "Elastic Cloud Storage Object Bucket");
		sdMetadata.put("providerDisplayName", "EMC Elastic Cloud Storage");
		sdMetadata.put("documentationUrl", "https://community.emc.com/docs/DOC-45012");
		sdMetadata.put("supportUrl", "http://www.emc.com/products-solutions/trial-software-download/ecs.htm");
		return sdMetadata;
	}

	private List<Plan> getBucketPlans() {
		Map<String, Object> plan1Meta = new HashMap<String, Object>();
		Map<String, Object> costs1Map = new HashMap<String, Object>();
		Map<String, Object> amount1Map = new HashMap<String, Object>();
		amount1Map.put("amount", new Double(0.0));
		costs1Map.put("amount", amount1Map);
		costs1Map.put("unit", "MONTHLY");
		plan1Meta.put("costs", Arrays.asList(costs1Map));
		plan1Meta.put("bullets", Arrays.asList("5 GB Storage", "Multi-protocol access:  S3, Swift, HDFS"));
		Plan plan1 = new Plan("ecs-bucket-small", "Small ECS Bucket", "10 GB ECS Bucket Plan", plan1Meta);

		Map<String, Object> plan2Meta = new HashMap<String, Object>();
		Map<String, Object> costs2Map = new HashMap<String, Object>();
		Map<String, Object> amount2Map = new HashMap<String, Object>();
		amount2Map.put("amount", new Double(0.03));
		costs2Map.put("amount", amount2Map);
		costs2Map.put("unit", "PER GB PER MONTH");
		plan2Meta.put("costs", Arrays.asList(costs2Map));
		plan2Meta.put("bullets", Arrays.asList("5 GB Storage", "Multi-protocol access:  S3, Swift, HDFS"));
		Plan plan2 = new Plan("ecs-bucket-unlimited", "Unlimited ECS Bucket", "Pay per GB for Month", plan2Meta);
		return Arrays.asList(plan1, plan2);
	}
}