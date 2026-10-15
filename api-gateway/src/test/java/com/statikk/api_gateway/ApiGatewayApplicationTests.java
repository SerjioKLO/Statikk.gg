package com.statikk.api_gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.gateway.discovery.locator.enabled=false",
        "spring.data.redis.repositories.enabled=false"
})
class ApiGatewayApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load Spring Cloud Gateway Application Context")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}
