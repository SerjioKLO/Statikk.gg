package com.statikk.matches;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MatchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchesApplication.class, args);
    }
}
