package com.statikk.excel_parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ExcelParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelParserApplication.class, args);
    }
}
