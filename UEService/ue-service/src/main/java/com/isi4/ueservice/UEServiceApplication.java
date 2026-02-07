package com.isi4.ueservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class UEServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UEServiceApplication.class, args);
    }
}
