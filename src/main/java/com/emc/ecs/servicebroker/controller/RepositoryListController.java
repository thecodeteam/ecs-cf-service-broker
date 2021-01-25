package com.emc.ecs.servicebroker.controller;

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

    /*
     * This method processes all requests sent on "/v2/repository/instances" and provides a list of Service Instances.
     * Also pagination is supported.
     * Method returns 200 OK on success and 500 Internal Server Error on error
     *
     * @param   marker      indicates the name of instance the page should start with (required: false)
     * @param   pageSize    states the amount of instances that would be presented in the output (default: 100)
     * @return              list of service instances
     */
    @GetMapping("/v2/repository/instances")
    public Mono<ListServiceInstancesResponse> getInstances(@RequestParam(name = "marker", required = false) String marker,
                                                           @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) throws IOException {
        Mono<ListServiceInstancesResponse> response = Mono.just(instanceRepository.listServiceInstances(marker, pageSize));
        return response
                .doOnRequest(v -> logger.info("Retrieving service instances"))
                .doOnSuccess(instancesResponse -> {
                    logger.info("Success retrieving service instances");
                    logger.debug("service instances = {}", instancesResponse);
                })
                .doOnError(e -> logger.error("Error retrieving service instances. Error = " + e.getMessage(), e));
    }

    /*
     * This method processes all requests sent on "/v2/repository/bindings" and provides a list of Service Instance Bindings.
     * Also pagination is supported.
     * Method returns 200 OK on success and 500 Internal Server Error on error
     *
     * @param   marker      indicates the name of binding the page should start with (required: false)
     * @param   pageSize    states the amount of bindings that would be presented in the output (default: 100)
     * @return              list of service instance bindings
     */
    @GetMapping("/v2/repository/bindings")
    public Mono<ListServiceInstanceBindingsResponse> getBindings(@RequestParam(name = "marker", required = false) String marker,
                                                                 @RequestParam(name = "pageSize", defaultValue = "100") int pageSize) throws IOException {
        Mono<ListServiceInstanceBindingsResponse> response = Mono.just(bindingRepository.listServiceInstanceBindings(marker, pageSize));
        return  response
                .doOnRequest(v -> logger.info("Retrieving service instance bindings"))
                .doOnSuccess(bindingsResponse -> {
                    logger.info("Success retrieving service instance bindings");
                    logger.debug("service instance bindings = {}", bindingsResponse);
                })
                .doOnError(e -> logger.error("Error retrieving service instance bindings. Error = " + e.getMessage(), e));
    }
}
