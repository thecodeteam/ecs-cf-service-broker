package com.emc.ecs.servicebroker.config;

import com.emc.ecs.servicebroker.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.annotation.ServiceBrokerRestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.io.IOException;

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
    public Mono<ListServiceInstancesResponse> getInstances(@RequestParam(name = "marker", required = false) String marker,
                                                           @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) throws IOException {
        return instanceRepository.listServiceInstances(marker, pageSize)
                .doOnRequest(v -> logger.info("Retrieving service instances"))
                .doOnSuccess(instancesResponse -> {
                    logger.info("Success retrieving service instances");
                    logger.debug("service instances = {}", instancesResponse);
                })
                .doOnError(e -> logger.error("Error retrieving service instances. Error = " + e.getMessage(), e));
    }

    @GetMapping("/v2/repository/bindings")
    public Mono<ListServiceInstanceBindingsResponse> getBindings(@RequestParam(name = "marker", required = false) String marker,
                                                                 @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) throws IOException {
        return bindingRepository.listServiceInstanceBindings(marker, pageSize)
                .doOnRequest(v -> logger.info("Retrieving service instance bindings"))
                .doOnSuccess(bindingsResponse -> {
                    logger.info("Success retrieving service instance bindings");
                    logger.debug("service instance bindings = {}", bindingsResponse);
                })
                .doOnError(e -> logger.error("Error retrieving service instance bindings. Error = " + e.getMessage(), e));
    }
}
