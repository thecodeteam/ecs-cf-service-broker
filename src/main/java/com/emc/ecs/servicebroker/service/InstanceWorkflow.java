package com.emc.ecs.servicebroker.service;

import com.emc.ecs.servicebroker.exception.EcsManagementClientException;
import com.emc.ecs.servicebroker.exception.EcsManagementResourceNotFoundException;
import com.emc.ecs.servicebroker.model.PlanProxy;
import com.emc.ecs.servicebroker.model.ServiceDefinitionProxy;
import com.emc.ecs.servicebroker.repository.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface InstanceWorkflow {
   InstanceWorkflow withCreateRequest(CreateServiceInstanceRequest request);
   InstanceWorkflow withDeleteRequest(DeleteServiceInstanceRequest request);
   Map<String, Object> changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan,
                                  Map<String, Object> parameters) throws EcsManagementClientException, ServiceBrokerException, IOException;

   /**
    * Perform the delete operation either async or sync
    * @return CompetableFuture if the delete is being performed async, otherwise null
    */
   CompletableFuture delete(String id) throws EcsManagementClientException, IOException, ServiceBrokerException;
   ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters)
           throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException;
}
