package com.isi4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "gateway");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @GetMapping("/services")
    public Map<String, Object> getRegisteredServices() {
        Map<String, Object> result = new HashMap<>();
        
        // Get all service names
        List<String> services = discoveryClient.getServices();
        result.put("totalServices", services.size());
        result.put("serviceNames", services);
        
        // Get details for each service
        Map<String, Object> serviceDetails = new HashMap<>();
        for (String serviceName : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            serviceDetails.put(serviceName, instances);
        }
        result.put("serviceDetails", serviceDetails);
        
        return result;
    }
    
    @GetMapping("/auth-service")
    public Map<String, Object> getAuthServiceInfo() {
        Map<String, Object> result = new HashMap<>();
        
        List<ServiceInstance> instances = discoveryClient.getInstances("authentification-service");
        result.put("found", !instances.isEmpty());
        result.put("instanceCount", instances.size());
        result.put("instances", instances);
        
        return result;
    }
}