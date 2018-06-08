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

public interface InstanceWorkflow {
   InstanceWorkflow withCreateRequest(CreateServiceInstanceRequest request);
   InstanceWorkflow withDeleteRequest(DeleteServiceInstanceRequest request);
   Map<String, Object> changePlan(String id, ServiceDefinitionProxy service, PlanProxy plan,
                                  Map<String, Object> parameters) throws EcsManagementClientException, ServiceBrokerException, IOException;
   void delete(String id) throws EcsManagementClientException, IOException, ServiceBrokerException;
   ServiceInstance create(String id, ServiceDefinitionProxy service, PlanProxy plan, Map<String, Object> parameters)
           throws EcsManagementClientException, EcsManagementResourceNotFoundException, IOException, JAXBException;
}
