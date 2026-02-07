package com.isi4.salleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SalleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalleServiceApplication.class, args);
    }
}