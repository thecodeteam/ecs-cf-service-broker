package com.emc.ecs.servicebroker.config;

import com.emc.ecs.servicebroker.repository.ServiceInstance;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBinding;
import com.emc.ecs.servicebroker.repository.ServiceInstanceBindingRepository;
import com.emc.ecs.servicebroker.repository.ServiceInstanceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.annotation.ServiceBrokerRestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ServiceBrokerRestController
public class RepositoryListController {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryListController.class);

    @Autowired
    private ServiceInstanceRepository instanceRepository;

    @Autowired
    private ServiceInstanceBindingRepository bindingRepository;

    public RepositoryListController() {
    }

    @GetMapping("/v2/repository/instances")
    public void getInstances(@RequestParam(name = "marker", required = false) String marker, @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) {
        try {
            List<ServiceInstance> instances = instanceRepository.listServiceInstances(marker, pageSize);
            for (ServiceInstance instance: instances) {
                logger.info("Info from instance: {}, service_instance_id: {}, service_id: {}, plan_id: {}, settings: {}",
                        instance.getName(), instance.getServiceInstanceId(), instance.getServiceDefinitionId(), instance.getPlanId(), instance.getServiceSettings());
            }
        } catch (Exception e) {
            //TODO: Add normal exception handling
            logger.error("Trouble: ");
            e.printStackTrace();
        }
    }

    @GetMapping("/v2/repository/bindings")
    public void getBindings(@RequestParam(name = "marker", required = false) String marker, @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) {
        try {
            List<ServiceInstanceBinding> bindings = bindingRepository.listServiceInstanceBindings(marker, pageSize);
            for (ServiceInstanceBinding binding: bindings) {
                logger.info("Info from instance: {}, binding_id: {}, service_id: {}, plan_id: {}, pars: {}",
                        binding.getName(), binding.getBindingId(), binding.getServiceDefinitionId(), binding.getPlanId(), binding.getParameters());
            }
        } catch (Exception e) {
            //TODO: Add normal exception handling
            logger.error("Trouble: ");
            e.printStackTrace();
        }
    }
}
