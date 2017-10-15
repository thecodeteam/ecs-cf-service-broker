package com.emc.ecs.cloudfoundry.broker.service;

import com.emc.ecs.cloudfoundry.broker.EcsManagementClientException;
import com.emc.ecs.cloudfoundry.broker.EcsManagementResourceNotFoundException;
import com.emc.ecs.cloudfoundry.broker.model.PlanProxy;
import com.emc.ecs.cloudfoundry.broker.model.ServiceDefinitionProxy;
import com.emc.ecs.cloudfoundry.broker.repository.ServiceInstance;
import org.springframework.cloud.servicebroker.exception.ServiceBrokerException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface InstanceWorkflow {
   InstanceWorkflow withCreateRequest(CreateServiceInstanceRequest request);
   InstanceWorkflow withDeleteRequest(DeleteServiceInstanceRequest request);
   void changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan,
       Optional<Map<String, Object>> maybeParameters) throws EcsManagementClientException, ServiceBrokerException, IOException;
   void delete(String id) throws EcsManagementClientException;
   ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan, Optional<Map<String, Object>> parameters)
           throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException;
}
